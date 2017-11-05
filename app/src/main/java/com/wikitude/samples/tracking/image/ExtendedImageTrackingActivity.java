package com.wikitude.samples.tracking.image;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRendererExtendedTracking;
import com.wikitude.samples.rendering.external.StrokedRectangle;
import com.wikitude.samples.util.DropDownAlert;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.ImageTracker;
import com.wikitude.tracker.ImageTrackerConfiguration;
import com.wikitude.tracker.ImageTrackerListener;
import com.wikitude.tracker.TargetCollectionResource;
import com.wikitude.tracker.TargetCollectionResourceLoadingCallback;

public class ExtendedImageTrackingActivity extends Activity implements ImageTrackerListener, ExternalRendering {

    private static final String TAG = "ExtendedTracking";

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mView;
    private Driver mDriver;
    private GLRendererExtendedTracking mGLRenderer;

    private ImageTracker mImageTracker;
    private TargetCollectionResource mTargetCollectionResource;

    private Button mStopExtendedTrackingButton;
    private DropDownAlert mDropDownAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);
        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/iot_tracker.wtc",
                new TargetCollectionResourceLoadingCallback() {
                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        Log.v(TAG, "Failed to load target collection resource. Reason: " + errorMessage);
                    }

                    @Override
                    public void onFinish() {
                        ImageTrackerConfiguration trackerConfiguration = new ImageTrackerConfiguration();
                        trackerConfiguration.setExtendedTargets(new String[]{"*"});
                        mImageTracker = mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, ExtendedImageTrackingActivity.this, trackerConfiguration);
                    }
                });

        mDropDownAlert = new DropDownAlert(this);
        mDropDownAlert.setText("Scan Target IOT:");
        mDropDownAlert.addImages("target_iot.jpg");
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
        mGLRenderer = new GLRendererExtendedTracking(renderExtension);
        mView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mDriver = new Driver(mView, 30);

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(mView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        RelativeLayout trackingQualityIndicator = (RelativeLayout) inflater.inflate(R.layout.activity_extended_tracking, null);
        viewHolder.addView(trackingQualityIndicator);

        mStopExtendedTrackingButton = (Button) findViewById(R.id.stop_extended_tracking_button);
        mStopExtendedTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageTracker.stopExtendedTracking();
            }
        });
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
        Log.v(TAG, "Recognized target " + target.getName());
        mDropDownAlert.dismiss();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStopExtendedTrackingButton.setVisibility(View.VISIBLE);
            }
        });

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
        Log.v(TAG, "Lost target " + target.getName());
        mGLRenderer.removeRenderablesForKey(target.getName() + target.getUniqueId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText trackingQualityIndicator = (EditText) findViewById(R.id.tracking_quality_indicator);
                trackingQualityIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onExtendedTrackingQualityChanged(ImageTracker tracker, final ImageTarget target, final int oldTrackingQuality, final int newTrackingQuality) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText trackingQualityIndicator = (EditText) findViewById(R.id.tracking_quality_indicator);
                switch (newTrackingQuality) {
                    case -1:
                        trackingQualityIndicator.setBackgroundColor(Color.parseColor("#FF3420"));
                        trackingQualityIndicator.setText(R.string.tracking_quality_indicator_bad);
                        break;
                    case 0:
                        trackingQualityIndicator.setBackgroundColor(Color.parseColor("#FFD900"));
                        trackingQualityIndicator.setText(R.string.tracking_quality_indicator_average);
                        break;
                    default:
                        trackingQualityIndicator.setBackgroundColor(Color.parseColor("#6BFF00"));
                        trackingQualityIndicator.setText(R.string.tracking_quality_indicator_good);
                }
                trackingQualityIndicator.setVisibility(View.VISIBLE);
            }
        });

    }
}
