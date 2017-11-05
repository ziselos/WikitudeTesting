package com.wikitude.samples.tracking.instant;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.wikitude.tracker.InstantTracker;
import com.wikitude.tracker.InstantTrackerListener;
import com.wikitude.tracker.InstantTarget;
import com.wikitude.tracker.InitializationPose;
import com.wikitude.tracker.InstantTrackingState;

public class InstantTrackingActivity extends Activity implements InstantTrackerListener, ExternalRendering {

    private static final String TAG = "InstantTracking";

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mSurfaceView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;

    private InstantTracker mInstantTracker;

    private InstantTrackingState mCurrentTrackingState = InstantTrackingState.Initializing;
    private InstantTrackingState mRequestedTrackingState = InstantTrackingState.Initializing;

    private LinearLayout mHeightSettingsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mInstantTracker = mWikitudeSDK.getTrackerManager().createInstantTracker(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWikitudeSDK.onResume();
        mSurfaceView.onResume();
        mDriver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWikitudeSDK.onPause();
        mSurfaceView.onPause();
        mDriver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        mGLRenderer = new GLRenderer(renderExtension_);
        mSurfaceView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mDriver = new Driver(mSurfaceView, 30);
        setContentView(mSurfaceView);

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(mSurfaceView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        RelativeLayout controls = (RelativeLayout) inflater.inflate(R.layout.activity_instant_tracking, null);
        viewHolder.addView(controls);

        mHeightSettingsLayout = (LinearLayout)findViewById(R.id.heightSettingsLayout);

        final Button changeStateButton = (Button) findViewById(R.id.on_change_tracker_state);
        changeStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mRequestedTrackingState == InstantTrackingState.Initializing) {
                    if (mCurrentTrackingState == InstantTrackingState.Initializing) {
                        mRequestedTrackingState = InstantTrackingState.Tracking;
                        mInstantTracker.setState(mRequestedTrackingState);
                        changeStateButton.setText(R.string.instant_tracking_button_start_initialization);
                    } else {
                        Log.e(TAG, "Tracker did not change state yet.");
                    }
                } else {
                    if (mCurrentTrackingState == InstantTrackingState.Tracking) {
                        mRequestedTrackingState = InstantTrackingState.Initializing;
                        mInstantTracker.setState(mRequestedTrackingState);
                        changeStateButton.setText(R.string.instant_tracking_button_start_tracking);
                    } else {
                        Log.e(TAG, "Tracker did not change state yet.");
                    }
                }
            }
        });

        final TextView heightBox = (TextView) findViewById(R.id.heightTextView);

        final SeekBar heightSlider = (SeekBar) findViewById(R.id.heightSeekBar);
        heightSlider.setMax(190);
        heightSlider.setProgress(90);
        heightSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float height = (progress + 10) / 100.f;
                mInstantTracker.setDeviceHeightAboveGround(height);
                heightBox.setText(String.format( "%.2f", height ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onStateChanged(InstantTracker tracker, InstantTrackingState state) {
        Log.v(TAG, "onStateChanged");
        mCurrentTrackingState = state;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // deviceHeightAboveGround may not be called during tracking
                mHeightSettingsLayout.setVisibility(mCurrentTrackingState == InstantTrackingState.Tracking ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onInitializationPoseChanged(InstantTracker tracker, InitializationPose pose) {
        Log.v(TAG, "onInitializationPoseChanged");

        StrokedRectangle strokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey("");
        if (strokedRectangle == null) {
            strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        }

        strokedRectangle.projectionMatrix = pose.getProjectionMatrix();
        strokedRectangle.viewMatrix = pose.getViewMatrix();

        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);

        LogMatrix("projectionMatrix", pose.getProjectionMatrix());
        LogMatrix("viewMatrix", pose.getViewMatrix());
    }

    @Override
    public void onTrackingStarted(InstantTracker tracker) {
        Log.v(TAG, "onTrackingStarted");
    }

    @Override
    public void onTracked(InstantTracker tracker, InstantTarget target) {
        StrokedRectangle strokedRectangle = (StrokedRectangle)mGLRenderer.getRenderableForKey("");
        if (strokedRectangle == null) {
            strokedRectangle = new StrokedRectangle(StrokedRectangle.Type.STANDARD);
        }

        strokedRectangle.projectionMatrix = target.getProjectionMatrix();
        strokedRectangle.viewMatrix = target.getViewMatrix();

        mGLRenderer.setRenderablesForKey("", strokedRectangle, null);

        LogMatrix("projectionMatrix", target.getProjectionMatrix());
        LogMatrix("viewMatrix", target.getViewMatrix());
    }

    @Override
    public void onTrackingStopped(InstantTracker tracker) {
        Log.v(TAG, "onTrackingStopped");
        mGLRenderer.removeRenderablesForKey("");
    }

    private void LogMatrix(String name, float[] matrix) {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(": ");
        for (float value : matrix) {
            builder.append(value);
            builder.append(" ");
        }
        Log.v(TAG, builder.toString());
    }
}
