package com.wikitude.samples.plugins;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import java.io.IOException;

public class WikitudeCamera implements Camera.ErrorCallback {

    public WikitudeCamera(int frameWidth, int frameHeight) {
        mFrameWidth = frameWidth;
        mFrameHeight = frameHeight;

    }

    public void start(Camera.PreviewCallback previewCallback) {
        try {
            mCamera = Camera.open(getCamera());
            mCamera.setErrorCallback(this);
            mCamera.setPreviewCallback(previewCallback);
            mCameraParameters = mCamera.getParameters();
            mCameraParameters.setPreviewFormat(ImageFormat.NV21);
            Camera.Size cameraSize = getCameraSize(mFrameWidth, mFrameHeight);
            mCameraParameters.setPreviewSize(cameraSize.width, cameraSize.height);
            mFieldOfView = mCameraParameters.getHorizontalViewAngle();
            mCamera.setParameters(mCameraParameters);
            mTexture = new SurfaceTexture(0);
            mCamera.setPreviewTexture((SurfaceTexture) mTexture);
            mCamera.startPreview();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            Log.e(TAG, "Camera not found: " + ex);
        }
    }

    public void close() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size getCameraSize(int desiredWidth, int desiredHeight) {
        for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
            if (size.width==desiredWidth && size.height==desiredHeight) {
                return size;
            }
        }
        return mCameraParameters.getSupportedPreviewSizes().get(0);
    }

    private int getCamera() {
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
                Camera.getCameraInfo(cameraId, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return cameraId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getImageSensorRotation() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        int cameraId = getCamera();

        if (cameraId != -1) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            int imageSensorRotation = cameraInfo.orientation;
            return 360 - imageSensorRotation; // the android API returns CW values (WHY?), 360 - X to have CCW
        } else {
            throw new RuntimeException("The getCamera function failed to return a valid camera ID. The image sensor rotation could therefore not be evaluated.");
        }
    }

    public double getCameraFieldOfView() { return mFieldOfView; }
    public int getFrameWidth() { return mFrameWidth; }
    public int getFrameHeight() { return mFrameHeight; }

    private static final String TAG = "WikitudeCamera";
    private int mFrameWidth;
    private int mFrameHeight;
    private double mFieldOfView;
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private Object mTexture;


}
