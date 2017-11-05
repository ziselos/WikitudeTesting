package com.wikitude.samples.tracking.image;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;
import com.wikitude.samples.rendering.external.StrokedRectangle;
import com.wikitude.samples.util.DropDownAlert;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.ImageTrackerListener;
import com.wikitude.tracker.TargetCollectionResource;
import com.wikitude.tracker.ImageTracker;
import com.wikitude.tracker.ImageTrackerConfiguration;
import com.wikitude.tracker.TargetCollectionResourceLoadingCallback;

import java.util.HashMap;

public class MultipleTargetsImageTrackingActivity extends Activity implements ImageTrackerListener, ExternalRendering {

    private static final String TAG = "MultipleTargetsActivity";

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;

    private TargetCollectionResource mTargetCollectionResource;
    private DropDownAlert mDropDownAlert;

    private final ImageTarget.OnDistanceBetweenTargetsListener mDistanceListener = new ImageTarget.OnDistanceBetweenTargetsListener() {
        @Override
        public void onDistanceBetweenTargetsChanged(int distance, ImageTarget firstTarget, ImageTarget secondTarget) {
            float r = 1.0f;
            float g = 0.58f;
            float b = 0.16f;

            if (distance < 300.0f) {
                if (firstTarget.getName().equals(secondTarget.getName())) {
                    r = 0.0f;
                    g = 0.0f;
                    b = 1.0f;
                } else {
                    r = 1.0f;
                    g = 0.0f;
                    b = 0.0f;
                }
            }

            StrokedRectangle firstStrokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey(firstTarget.getName() + firstTarget.getUniqueId());
            if (firstStrokedRectangle != null) {
                firstStrokedRectangle.setColor(r, g, b);
            }

            StrokedRectangle secondStrokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey(secondTarget.getName() + secondTarget.getUniqueId());
            if (secondStrokedRectangle != null) {
                secondStrokedRectangle.setColor(r, g, b);
            }
        }
    };


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);
        startupConfiguration.setCameraResolution(CameraSettings.CameraResolution.AUTO);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/magazine.wtc", new TargetCollectionResourceLoadingCallback() {
                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        Log.v(TAG, "Failed to load target collection resource. Reason: " + errorMessage);
                    }

                    @Override
                    public void onFinish() {
                        HashMap<String, Integer> physicalTargetImageHeights = new HashMap<>();
                        physicalTargetImageHeights.put("pageOne", 252);
                        physicalTargetImageHeights.put("pageTwo", 252);

                        ImageTrackerConfiguration trackerConfiguration = new ImageTrackerConfiguration();
                        trackerConfiguration.setMaximumNumberOfConcurrentlyTrackableTargets(5);
                        trackerConfiguration.setDistanceChangedThreshold(10);
                        trackerConfiguration.setPhysicalTargetImageHeights(physicalTargetImageHeights);

                        ImageTracker tracker = mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, MultipleTargetsImageTrackingActivity.this, trackerConfiguration);
                    }
                });

        mDropDownAlert = new DropDownAlert(this);
        mDropDownAlert.setText("Scan Target #1 (surfer):");
        mDropDownAlert.addImages("surfer.png");
        mDropDownAlert.setTextWeight(0.5f);
        mDropDownAlert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWikitudeSDK.onResume();
        mView.onResume();
        mDriver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWikitudeSDK.onPause();
        mView.onPause();
        mDriver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.clearCache();
        mWikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension) {
        mGLRenderer = new GLRenderer(renderExtension);
        mView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mDriver = new Driver(mView, 30);
        setContentView(mView);
    }

    @Override
    public void onTargetsLoaded(ImageTracker tracker) {
        Log.v(TAG, "Image tracker loaded");
    }

    @Override
    public void onErrorLoadingTargets(ImageTracker tracker, int errorCode, final String errorMessage) {
        Log.v(TAG, "Unable to load image tracker. Reason: " + errorMessage);
    }

    @Override
    public void onImageRecognized(ImageTracker tracker, final ImageTarget target) {
        Log.v(TAG, "Recognized target " + target.getName() + target.getUniqueId());
        target.setOnDistanceBetweenTargetsListener(mDistanceListener);
        mDropDownAlert.dismiss();

        StrokedRectangle strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        mGLRenderer.setRenderablesForKey(target.getName() + target.getUniqueId(), strokedRectangle, null);
    }

    @Override
    public void onImageTracked(ImageTracker tracker, final ImageTarget target) {
        StrokedRectangle strokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey(target.getName() + target.getUniqueId());

        if (strokedRectangle != null) {
            strokedRectangle.projectionMatrix = target.getProjectionMatrix();
            strokedRectangle.viewMatrix = target.getViewMatrix();

            strokedRectangle.setXScale(target.getTargetScale().x);
            strokedRectangle.setYScale(target.getTargetScale().y);
        }
    }

    @Override
    public void onImageLost(ImageTracker tracker, final ImageTarget target) {
        Log.v(TAG, "Lost target " + target.getName() + target.getUniqueId());
        target.setOnDistanceBetweenTargetsListener(null);
        mGLRenderer.removeRenderablesForKey(target.getName() + target.getUniqueId());
    }

    @Override
    public void onExtendedTrackingQualityChanged(ImageTracker tracker, final ImageTarget target, final int oldTrackingQuality, final int newTrackingQuality) {

    }
}
