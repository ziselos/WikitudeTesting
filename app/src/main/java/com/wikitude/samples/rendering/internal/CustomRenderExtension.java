package com.wikitude.samples.rendering.internal;

import android.opengl.GLSurfaceView;

import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.samples.rendering.external.StrokedRectangle;
import com.wikitude.tracker.ImageTarget;
import com.wikitude.tracker.Target;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CustomRenderExtension implements GLSurfaceView.Renderer, RenderExtension {

    private Target mCurrentlyRecognizedTarget = null;
    private StrokedRectangle mStrokedRectangle;

    @Override
    public void onDrawFrame(final GL10 unused) {
        if (mCurrentlyRecognizedTarget != null) {
            mStrokedRectangle.projectionMatrix = mCurrentlyRecognizedTarget.getProjectionMatrix();
            mStrokedRectangle.viewMatrix = mCurrentlyRecognizedTarget.getViewMatrix();
            mStrokedRectangle.onDrawFrame();
        }
    }

    public void onUpdate() {

    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        mStrokedRectangle = new StrokedRectangle();
        mStrokedRectangle.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
    }

    public void onResume() {
    }

    @Override
    public void useSeparatedRenderAndLogicUpdates() {

    }

    public void onPause() {
    }

    public void setCurrentlyRecognizedTarget(final ImageTarget currentlyRecognizedTarget) {
        mCurrentlyRecognizedTarget = currentlyRecognizedTarget;
    }

}
