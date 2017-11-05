package com.wikitude.samples.plugins;

import com.wikitude.NativeStartupConfiguration;
import com.wikitude.WikitudeSDK;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.plugins.PluginManager;
import com.wikitude.common.rendering.RenderExtension;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;

import java.nio.ByteBuffer;

public class SimpleInputPluginActivity extends Activity implements ImageTrackerListener, ExternalRendering {

    private static final String TAG = "SimpleInputPlugin";

    private WikitudeSDK mWikitudeSDK;
    private CustomSurfaceView mView;
    private Driver mDriver;
    private GLRenderer mGLRenderer;
    private WikitudeCamera2 mWikitudeCamera2;
    private WikitudeCamera mWikitudeCamera;

    private TargetCollectionResource mTargetCollectionResource;
    private DropDownAlert mDropDownAlert;

    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // new instance of the WikitudeSDK with ExternalRendering
        mWikitudeSDK = new WikitudeSDK(this);

        // creating configuration for the SDK
        NativeStartupConfiguration startupConfiguration = new NativeStartupConfiguration();
        startupConfiguration.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY);
        startupConfiguration.setCameraPosition(CameraSettings.CameraPosition.BACK);

        // wikitude SDK will be created with the given configuration
        mWikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        // creating a new TargetCollectionResource from a .wtc file containing information about the image to track
        mTargetCollectionResource = mWikitudeSDK.getTrackerManager().createTargetCollectionResource("file:///android_asset/magazine.wtc", new TargetCollectionResourceLoadingCallback() {
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.v(TAG, "Failed to load target collection resource. Reason: " + errorMessage);
            }

            @Override
            public void onFinish() {
                // creating a new ImageTracker with the loaded TargetCollectionResource and
                // a ImageTrackerListener which gets all necessary callbacks from the ImageTracker
                mWikitudeSDK.getTrackerManager().createImageTracker(mTargetCollectionResource, SimpleInputPluginActivity.this, null);
            }
        });

        // register Plugin in the wikitude SDK and in the jniRegistration.cpp
        mWikitudeSDK.getPluginManager().registerNativePlugins("wikitudePlugins", "simple_input_plugin", new PluginManager.PluginErrorCallback() {
            @Override
            public void onRegisterError(int errorCode, String errorMessage) {
                Log.v(TAG, "Plugin failed to load. Reason: " + errorMessage);
            }
        });

        // sets this activity in the plugin
        initNative();

        setFrameSize(FRAME_WIDTH, FRAME_HEIGHT);

        // alert showing which target image to scan
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
    public void onErrorLoadingTargets(ImageTracker tracker, int errorCode, final String errorMessage) {
        Log.v(TAG, "Unable to load image tracker. Reason: " + errorMessage);
    }

    @Override
    public void onTargetsLoaded(ImageTracker tracker) {
        Log.v(TAG, "Image tracker loaded");
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
        Log.v(TAG, "Image tracked " + target.getName());
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Called from c++ on initialization of the Plugin.
     */
    @SuppressWarnings("unused")
    public void onInputPluginInitialized() {
        Log.v(TAG, "onInputPluginInitialized");

        setFrameSize(FRAME_WIDTH, FRAME_HEIGHT);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    mWikitudeCamera2 = new WikitudeCamera2(SimpleInputPluginActivity.this, FRAME_WIDTH, FRAME_HEIGHT);
                    setCameraFieldOfView((mWikitudeCamera2.getCameraFieldOfView()));

                    int imageSensorRotation = mWikitudeCamera2.getImageSensorRotation();
                    if (imageSensorRotation != 0) {
                        setImageSensorRotation(imageSensorRotation);
                    }
                }
                else
                {
                    mWikitudeCamera = new WikitudeCamera(FRAME_WIDTH, FRAME_HEIGHT);

                    int imageSensorRotation = mWikitudeCamera.getImageSensorRotation();
                    if (imageSensorRotation != 0) {
                        setImageSensorRotation(imageSensorRotation);
                    }
                }
            }
        });
    }

    /**
     * Called from c++ on pause of the Plugin.
     */
    @SuppressWarnings("unused")
    public void onInputPluginPaused() {
        Log.v(TAG, "onInputPluginPaused");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
                {
                    mWikitudeCamera2.close();
                }
                else
                {
                    mWikitudeCamera.close();
                }
            }
        });
    }

    /**
     * Called from c++ on resume of the Plugin.
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("unused")
    public void onInputPluginResumed() {
        Log.v(TAG, "onInputPluginResumed");


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SimpleInputPluginActivity.this.mWikitudeCamera2.start(new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Image image = reader.acquireLatestImage();

                            if (null != image && null != image.getPlanes()) {
                                Image.Plane[] planes = image.getPlanes();

                                int widthLuminance = image.getWidth();
                                int heightLuminance = image.getHeight();

                                // 4:2:0 format -> chroma planes have half the width and half the height of the luma plane
                                int widthChrominance = widthLuminance / 2;
                                int heightChrominance = heightLuminance / 2;

                                int pixelStrideLuminance = planes[0].getPixelStride();
                                int rowStrideLuminance = planes[0].getRowStride();

                                int pixelStrideBlue = planes[1].getPixelStride();
                                int rowStrideBlue = planes[1].getRowStride();

                                int pixelStrideRed = planes[2].getPixelStride();
                                int rowStrideRed = planes[2].getRowStride();

                                notifyNewCameraFrame(
                                        widthLuminance,
                                        heightLuminance,
                                        getPlanePixelPointer(planes[0].getBuffer()),
                                        pixelStrideLuminance,
                                        rowStrideLuminance,
                                        widthChrominance,
                                        heightChrominance,
                                        getPlanePixelPointer(planes[1].getBuffer()),
                                        pixelStrideBlue,
                                        rowStrideBlue,
                                        getPlanePixelPointer(planes[2].getBuffer()),
                                        pixelStrideRed,
                                        rowStrideRed
                                );

                                image.close();
                            }
                        }
                    });
                }
                else
                {
                    //noinspection deprecation
                    mWikitudeCamera.start(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            notifyNewCameraFrameN21(data);
                        }
                    });
                    setCameraFieldOfView(mWikitudeCamera.getCameraFieldOfView());
                }
            }
        });
    }

    /**
     * Called from c++ on destroy of the Plugin.
     */
    @SuppressWarnings("unused")
    public void onInputPluginDestroyed() {
        Log.v(TAG, "onInputPluginDestroyed");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    mWikitudeCamera2.close();
                }
                else
                {
                    mWikitudeCamera.close();
                }
            }
        });
    }

    private byte[] getPlanePixelPointer(ByteBuffer pixelBuffer) {
        byte[] bytes;
        if (pixelBuffer.hasArray()) {
            bytes = pixelBuffer.array();
        } else {
            bytes = new byte[pixelBuffer.remaining()];
            pixelBuffer.get(bytes);
        }

        return bytes;
    }

    private native void initNative();
    private native void notifyNewCameraFrame(int widthLuminance, int heightLuminance, byte[] pixelPointerLuminance, int pixelStrideLuminance, int rowStrideLuminance, int widthChrominance, int heightChrominance, byte[] pixelPointerChromaBlue, int pixelStrideBlue, int rowStrideBlue, byte[] pixelPointerChromaRed, int pixelStrideRed, int rowStrideRed);
    private native void notifyNewCameraFrameN21(byte[] frameData);
    private native void setCameraFieldOfView(double fieldOfView);
    private native void setFrameSize(int frameWidth, int frameHeight);
    private native void setImageSensorRotation(int rotation);
}
