package com.wikitude.samples.camera;

import com.wikitude.WikitudeSDK;
import com.wikitude.NativeStartupConfiguration;
import com.wikitude.camera.CameraManagerListener;
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
import com.wikitude.tracker.TargetCollectionResource;
import com.wikitude.tracker.TargetCollectionResourceLoadingCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class CameraSettingsActivity extends Activity implements ExternalRendering, AdapterView.OnItemSelectedListener, CameraManagerListener, ImageTrackerListener {

    private static final String TAG = "CameraControlsActivity";

    private WikitudeSDK mWikitudeSDK;
    private GLRenderer mGLRenderer;
    private CustomSurfaceView mCustomSurfaceView;
    private Driver mDriver;

    private boolean mIsCameraOpen;

    private TargetCollectionResource mTargetCollectionResource;
    private TableRow focusRow;
    private boolean mCamera2Enabled = true;
    private DropDownAlert mDropDownAlert;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCamera2Enabled = getIntent().getBooleanExtra("enableCamera2", true);

        mWikitudeSDK = new WikitudeSDK(this);
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);
        startupConfiguration.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        startupConfiguration.setCamera2Enabled(mCamera2Enabled);
        mWikitudeSDK.getCameraManager().setListener(this);

        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/magazine.wtc", new TargetCollectionResourceLoadingCallback() {
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.v(TAG, "Failed to load target collection resource. Reason: " + errorMessage);
            }

            @Override
            public void onFinish() {
                mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, CameraSettingsActivity.this, null);
            }
        });
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
        mWikitudeSDK.clearCache();
        mWikitudeSDK.onDestroy();
    }


    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension) {
        mGLRenderer = new GLRenderer(renderExtension);
        mCustomSurfaceView = new CustomSurfaceView(getApplicationContext(), mGLRenderer);
        mDriver = new Driver(mCustomSurfaceView, 30);
    }

    @Override
    public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
        if (mIsCameraOpen) {
            switch (adapterView.getId()) {
                case R.id.focusMode:
                    switch (position){
                        case 0:
                            mWikitudeSDK.getCameraManager().setFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
                            if (focusRow != null) {
                                focusRow.setVisibility(View.GONE);
                            }
                            break;
                        case 1:
                            mWikitudeSDK.getCameraManager().setFocusMode(CameraSettings.CameraFocusMode.ONCE);
                            if (focusRow != null) {
                                focusRow.setVisibility(View.GONE);
                            }
                            break;
                        case 2:
                            mWikitudeSDK.getCameraManager().setFocusMode(CameraSettings.CameraFocusMode.OFF);
                            if (mWikitudeSDK.getCameraManager().isManualFocusAvailable()) {
                                if (focusRow != null) {
                                    focusRow.setVisibility(View.VISIBLE);
                                }
                            } else if (!mCamera2Enabled || Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                Toast.makeText(this, "Manual Focus is not supported by the old camera API. The focus will be fixed at infinity focus if the device supports it.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Manual Focus is not supported by this device. The focus will be fixed at infinity focus.", Toast.LENGTH_SHORT).show();
                            }
                    }
                    break;
                case R.id.cameraPosition:
                    if (position == 0) {
                        mWikitudeSDK.getCameraManager().setCameraPosition(CameraSettings.CameraPosition.BACK);
                    } else {
                        mWikitudeSDK.getCameraManager().setCameraPosition(CameraSettings.CameraPosition.FRONT);
                    }
                    break;
            }
        } else {
            Log.e("CAMERA_OPEN", "camera is not open");
        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> adapterView) {

    }

    @Override
    public void onCameraOpen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsCameraOpen) {
                    final FrameLayout viewHolder = new FrameLayout(getApplicationContext());
                    setContentView(viewHolder);

                    viewHolder.addView(mCustomSurfaceView);

                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    final FrameLayout controls = (FrameLayout) inflater.inflate(R.layout.activity_camera_control, null);
                    viewHolder.addView(controls);

                    Spinner cameraPositionSpinner = (Spinner) findViewById(R.id.cameraPosition);
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(CameraSettingsActivity.this, R.array.camera_positions, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cameraPositionSpinner.setAdapter(adapter);
                    cameraPositionSpinner.setOnItemSelectedListener(CameraSettingsActivity.this);

                    Spinner focusModeSpinner = (Spinner) findViewById(R.id.focusMode);
                    adapter = ArrayAdapter.createFromResource(CameraSettingsActivity.this, R.array.focus_mode, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    focusModeSpinner.setAdapter(adapter);
                    focusModeSpinner.setOnItemSelectedListener(CameraSettingsActivity.this);

                    Switch flashToggleButton = (Switch) findViewById(R.id.flashlight);
                    flashToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                            if (isChecked) {
                                mWikitudeSDK.getCameraManager().enableCameraFlashLight();
                            } else {
                                mWikitudeSDK.getCameraManager().disableCameraFlashLight();
                            }

                        }
                    });

                    SeekBar zoomSeekBar = (SeekBar) findViewById(R.id.zoomSeekBar);
                    zoomSeekBar.setMax(((int) mWikitudeSDK.getCameraManager().getMaxZoomLevel()) * 100);
                    zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                            if (progress > 0) {
                                mWikitudeSDK.getCameraManager().setZoomLevel((float) progress / 100.0f);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(final SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(final SeekBar seekBar) {

                        }
                    });

                    focusRow = (TableRow) findViewById(R.id.tableRow4);
                    
                    SeekBar focusSeekBar = (SeekBar) findViewById(R.id.focusSeekBar);
                    focusSeekBar.setMax(100);
                    focusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                            mWikitudeSDK.getCameraManager().setManualFocusDistance((float)progress/100.0f);
                        }

                        @Override
                        public void onStartTrackingTouch(final SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(final SeekBar seekBar) {
                        }
                    });

                    final TableLayout tableLayout = (TableLayout)findViewById(R.id.tableLayout1);
                    tableLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mDropDownAlert = new DropDownAlert(CameraSettingsActivity.this);
                            mDropDownAlert.setText("Scan Target #1 (surfer):");
                            mDropDownAlert.addImages("surfer.png");
                            mDropDownAlert.setTextWeight(0.5f);
                            mDropDownAlert.setMarginTop(tableLayout.getHeight());
                            mDropDownAlert.show(viewHolder);
                            controls.bringToFront();
                        }
                    });
                }
                mIsCameraOpen = true;
            }
        });
    }

    @Override
    public void onCameraReleased() {
    }

    @Override
    public void onCameraOpenFailure() {
        /*
            This is a workaround for some devices whose camera2 implementation is not working as expected.
        */
        if (mCamera2Enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            AlertDialog alertDialog = new AlertDialog.Builder(CameraSettingsActivity.this).create();
            alertDialog.setTitle("Camera2 issue.");
            alertDialog.setMessage("There was an unexpected issue with this devices camera2. Should this activity be recreated with the old camera api?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(CameraSettingsActivity.this, CameraSettingsActivity.class);
                    intent.putExtra("enableCamera2", false);
                    finish();
                    startActivity(intent);
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    });
                }
            });
            alertDialog.show();
        } else {
            Toast.makeText(this, "Camera could not be started.", Toast.LENGTH_SHORT).show();
        }
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
    }

    @Override
    public void onExtendedTrackingQualityChanged(ImageTracker tracker, final ImageTarget target, final int oldTrackingQuality, final int newTrackingQuality) {

    }
}
