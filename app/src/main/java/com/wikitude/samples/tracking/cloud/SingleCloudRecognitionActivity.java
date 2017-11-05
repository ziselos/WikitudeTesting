package com.wikitude.samples.tracking.cloud;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;
import com.wikitude.samples.rendering.external.StrokedRectangle;
import com.wikitude.samples.util.DropDownAlert;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.ImageTracker;
import com.wikitude.tracker.ImageTrackerListener;
import com.wikitude.tracker.CloudRecognitionService;
import com.wikitude.tracker.CloudRecognitionServiceResponse;
import com.wikitude.tracker.CloudRecognitionServiceInitializationCallback;
import com.wikitude.tracker.CloudRecognitionServiceListener;

public class SingleCloudRecognitionActivity extends Activity implements ImageTrackerListener, ExternalRendering {

    private static final String TAG = "OnClickCloudTracking";

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mCustomSurfaceView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;

    private CloudRecognitionService mCloudRecognitionService;
    private DropDownAlert mDropDownAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);


        mCloudRecognitionService = mWikitudeSDK.getTrackerManager().createCloudRecognitionService("b277eeadc6183ab57a83b07682b3ceba", "54e4b9fe6134bb74351b2aa3", new CloudRecognitionServiceInitializationCallback() {
            @Override
            public void onInitialized() {
                mWikitudeSDK.getTrackerManager().createImageTracker(mCloudRecognitionService, SingleCloudRecognitionActivity.this, null);
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.e(TAG, "Cloud Recognition Service failed to initialize. Reason: " + errorMessage);
            }
        });

        mDropDownAlert = new DropDownAlert(this);
        mDropDownAlert.setText("Scan:");
        mDropDownAlert.addImages("austria.jpg", "brazil.jpg", "france.jpg", "germany.jpg", "italy.jpg");
        mDropDownAlert.setTextWeight(0.2f);
        mDropDownAlert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWikitudeSDK.onResume();
        mCustomSurfaceView.onResume();
        mDriver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWikitudeSDK.onPause();
        mCustomSurfaceView.onPause();
        mDriver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension) {
        mGLRenderer = new GLRenderer(renderExtension);
        mCustomSurfaceView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mDriver = new Driver(mCustomSurfaceView, 30);

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(mCustomSurfaceView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        LinearLayout controls = (LinearLayout) inflater.inflate(R.layout.activity_on_click_cloud_tracking, null);
        viewHolder.addView(controls);

        Button recognizeButton = (Button) findViewById(R.id.on_click_cloud_tracking_recognize_button);
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view_) {
                mCloudRecognitionService.recognize(new CloudRecognitionServiceListener() {
                    @Override
                    public void onResponse(final CloudRecognitionServiceResponse response) {
                        if (response.isRecognized()) {
                            mDropDownAlert.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    EditText targetInformationTextField = (EditText) findViewById(R.id.on_click_cloud_tracking_info_field);
                                    targetInformationTextField.setText(response.getTargetInformations().get("name"), TextView.BufferType.NORMAL);
                                    targetInformationTextField.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    EditText targetInformationTextField = (EditText) findViewById(R.id.on_click_cloud_tracking_info_field);
                                    targetInformationTextField.setText("Recognition failed - Please try again", TextView.BufferType.NORMAL);
                                    targetInformationTextField.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(final int errorCode, final String errorMessage) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EditText targetInformationTextField = (EditText) findViewById(R.id.on_click_cloud_tracking_info_field);
                                targetInformationTextField.setText("Recognition failed - Error code: " + errorCode + " Message: " + errorMessage);
                                targetInformationTextField.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
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
    public void onImageRecognized(final ImageTracker tracker, final ImageTarget target) {
        Log.v(TAG, "Recognized target " + target.getName());

        StrokedRectangle strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        mGLRenderer.setRenderablesForKey(target.getName() + target.getUniqueId(), strokedRectangle, null);
    }

    @Override
    public void onImageTracked(final ImageTracker tracker, final ImageTarget target) {
        StrokedRectangle strokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey(target.getName() + target.getUniqueId());

        if (strokedRectangle != null) {
            strokedRectangle.projectionMatrix = target.getProjectionMatrix();
            strokedRectangle.viewMatrix = target.getViewMatrix();

            strokedRectangle.setXScale(target.getTargetScale().x);
            strokedRectangle.setYScale(target.getTargetScale().y);
        }
    }

    @Override
    public void onImageLost(final ImageTracker tracker, final ImageTarget target) {
        Log.v(TAG, "Lost target " + target.getName());
        mGLRenderer.removeRenderablesForKey(target.getName() + target.getUniqueId());
    }

    @Override
    public void onExtendedTrackingQualityChanged(final ImageTracker tracker, final ImageTarget target, final int oldTrackingQuality, final int newTrackingQuality) {

    }
}
