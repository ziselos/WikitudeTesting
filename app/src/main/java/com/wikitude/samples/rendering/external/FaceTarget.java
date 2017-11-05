package com.wikitude.samples.rendering.external;

public class FaceTarget {
    private float[] mProjectionMatrix;
    private float[] mViewMatrix;

    public FaceTarget() {
        mProjectionMatrix = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        mViewMatrix = new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    }

    public FaceTarget(float[] projectionMatrix, float[] viewMatrix) {
        mProjectionMatrix = projectionMatrix;
        mViewMatrix = viewMatrix;
    }

    public float[] getProjectionMatrix() {
        return mProjectionMatrix;
    }

    public void setProjectionMatrix(float[] projectionMatrix) {
        mProjectionMatrix = projectionMatrix;
    }

    public float[] getViewMatrix() {
        return mViewMatrix;
    }

    public void setViewMatrix(float[] viewMatrix) {
        mViewMatrix = viewMatrix;
    }
}
