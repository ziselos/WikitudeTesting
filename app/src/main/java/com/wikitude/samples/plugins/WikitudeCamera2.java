package com.wikitude.samples.plugins;

/**
 * Created by danielguttenberg on 23/03/16.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;

@TargetApi(22)
public class WikitudeCamera2
{
    public WikitudeCamera2(Context context, int frameWidth, int frameHeight) {
        mContext = context;

        mFrameWidth = frameWidth;
        mFrameHeight = frameHeight;

        mManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        mFieldOfView = getCameraFieldOfViewInternal();
        mImageSensorRotation = getImageSensorRotationInternal();

        mCloseCalled = false;
    }

    public void start(ImageReader.OnImageAvailableListener onImageAvailableListener) {
        try {
            if (Build.VERSION.SDK_INT >= 23 && mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Camera Permission has been denied by the user. Aborting initialization.");
                throw new SecurityException();
            }

            mManager.openCamera(getCamera(), cameraStateCallback, null);
            mImageReader = ImageReader.newInstance(mFrameWidth, mFrameHeight, ImageFormat.YUV_420_888, 2);
            mImageReader.setOnImageAvailableListener(onImageAvailableListener, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        synchronized (mCameraClosedLock) {
            mCloseCalled = true;
            try {
                if (mCameraCaptureSession != null && mCameraDevice != null) {
                    mCloseCalled = false;
                }

                if (mCameraCaptureSession != null) {
                    mCameraCaptureSession.abortCaptures();
                    mCameraCaptureSession.close();
                    mCameraCaptureSession = null;
                }

                if (mCameraDevice != null) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }

                if (mImageReader != null) {
                    mImageReader.close();
                    mImageReader = null;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /*private void process(Image image)
    {
        Log.i(TAG, "width: " + image.getWidth());
        Log.i(TAG, "height: " + image.getHeight());
        Log.i(TAG, "format: " + image.getFormat());
        Log.i(TAG, "timestamp: " + image.getTimestamp());

        Image.Plane[] YCbCr = image.getPlanes();
        ByteBuffer Y = YCbCr[0].getBuffer();
        ByteBuffer Cb = YCbCr[1].getBuffer();
        ByteBuffer Cr = YCbCr[2].getBuffer();

        // do something with YCbCr image data
    }*/

    private String getCamera() {
        try {
            for (String cameraId : mManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = mManager.getCameraCharacteristics(cameraId);

                int cameraOrientation = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    float sensorWidth = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth();
                    float focalLength = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
                    mFieldOfView = Math.toDegrees(2 * Math.atan(0.5 * sensorWidth / focalLength));

                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private double getCameraFieldOfViewInternal() {
        try {
            for (String cameraId : mManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = mManager.getCameraCharacteristics(cameraId);

                int cameraOrientation = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    float sensorWidth = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth();
                    float focalLength = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
                    return Math.toDegrees(2 * Math.atan(0.5 * sensorWidth / focalLength));
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return 0.0f;
    }

    private int getImageSensorRotationInternal() {
        try {
            if (mManager.getCameraIdList().length == 0) {
                throw new RuntimeException("The camera manager returned an empty list of available cameras. The image sensor rotation could not be evaluated.");
            } else {
                for (String cameraId : mManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics = mManager.getCameraCharacteristics(cameraId);

                    int cameraOrientation = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                        return 360 - sensorOrientation; // the android API returns CW values (WHY?), 360 - X to have CCW
                    } else {
                        throw new RuntimeException("No back facing camera found. The image sensor rotation could not be evaluated.");
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // 90, 180, 270, 360 are valid values
        // as this function return an angle in degrees that is used to rotate the camera image
        // a visually easily perceivable values is chosen. Using -1 might go unnoticed as
        // a rotation this small is visually insignificant.
        return 45;
    }

    private CaptureRequest createCaptureRequest() {
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(mImageReader.getSurface());
            return builder.build();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            synchronized (mCameraClosedLock) {
                if (!mCloseCalled) {
                    mCameraDevice = camera;
                    try {
                        mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()), sessionStateCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    mCloseCalled = false;
                }
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.e(TAG, "Callback function onDisconnected called.");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            if (mCloseCalled) {
                mCloseCalled = false;
            }

            String errorString = "";
            switch (error) {
                case ERROR_CAMERA_DEVICE:
                    errorString = "ERROR_CAMERA_DEVICE received, indicating that the camera device has encountered a fatal error.";
                    break;
                case ERROR_CAMERA_DISABLED:
                    errorString = "ERROR_CAMERA_DISABLED received, indicating that the camera device could not be opened due to a device policy.";
                    break;
                case ERROR_CAMERA_IN_USE:
                    errorString = "ERROR_CAMERA_IN_USE received, indicating that the camera device is in use already.";
                    break;
                case ERROR_CAMERA_SERVICE:
                    errorString = "ERROR_CAMERA_SERVICE received, indicating that the camera service has encountered a fatal error.";
                    break;
                case ERROR_MAX_CAMERAS_IN_USE:
                    errorString = "ERROR_MAX_CAMERAS_IN_USE received, indicating that the camera device could not be opened because there are too many other open camera devices.";
                    break;
            }

            Log.e(TAG, "Callback function onError called." + errorString);
        }
    };

    private CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            synchronized (mCameraClosedLock) {
                if (!mCloseCalled) {
                    mCameraCaptureSession = session;
                    try {
                        session.setRepeatingRequest(createCaptureRequest(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    mCloseCalled = false;
                }
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            if (mCloseCalled) {
                mCloseCalled = false;
            }
        }
    };

    public double getCameraFieldOfView() { return mFieldOfView; }
    public int getImageSensorRotation() { return mImageSensorRotation; }
    public int getFrameWidth() { return mFrameWidth; }
    public int getFrameHeight() { return mFrameHeight; }

    private static final String TAG = "WikitudeCamera2";
    private Context mContext;
    private int mFrameWidth;
    private int mFrameHeight;
    CameraManager mManager;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private double mFieldOfView;
    private int mImageSensorRotation;
    private boolean mCloseCalled;
    private final Object mCameraClosedLock = new Object();
}