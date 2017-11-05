package com.wikitude.samples.rendering.external;

import com.wikitude.common.rendering.RenderExtension;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRendererFaceDetectionPlugin extends GLRenderer {

    private StrokedRectangle mStrokedRectangle;
    private FaceTarget mCurrentlyRecognizedFace;


    public GLRendererFaceDetectionPlugin(RenderExtension wikitudeRenderExtension_) {
        super(wikitudeRenderExtension_);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        super.onSurfaceCreated(unused, config);
        mStrokedRectangle = new StrokedRectangle(StrokedRectangle.Type.FACE);
        mStrokedRectangle.onSurfaceCreated();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        super.onDrawFrame(unused);
        if (mCurrentlyRecognizedFace != null) {
            mStrokedRectangle.projectionMatrix = mCurrentlyRecognizedFace.getProjectionMatrix();
            mStrokedRectangle.viewMatrix = mCurrentlyRecognizedFace.getViewMatrix();
            mStrokedRectangle.onDrawFrame();
        }
    }

    public void setCurrentlyRecognizedFace(FaceTarget face) {
        mCurrentlyRecognizedFace = face;
    }

}
