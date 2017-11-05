package com.wikitude.samples.rendering.external;

import com.wikitude.common.rendering.RenderExtension;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.util.TreeMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {

    private RenderExtension mWikitudeRenderExtension = null;
    private TreeMap<String, Renderable> mOccluders = new TreeMap<>();
    private TreeMap<String, Renderable> mRenderables = new TreeMap<>();

    public GLRenderer(RenderExtension wikitudeRenderExtension) {
        mWikitudeRenderExtension = wikitudeRenderExtension;
        /*
         * Until Wikitude SDK version 2.1 onDrawFrame triggered also a logic update inside the SDK core.
         * This behaviour is deprecated and onUpdate should be used from now on to update logic inside the SDK core. <br>
         *
         * The default behaviour is that onDrawFrame also updates logic. <br>
         *
         * To use the new separated drawing and logic update methods, RenderExtension.useSeparatedRenderAndLogicUpdates should be called.
         * Otherwise the logic will still be updated in onDrawFrame.
         */
        mWikitudeRenderExtension.useSeparatedRenderAndLogicUpdates();
    }

    @Override
    public synchronized void onDrawFrame(final GL10 unused) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mWikitudeRenderExtension != null) {
            // Will trigger a logic update in the SDK
            mWikitudeRenderExtension.onUpdate();
            // will trigger drawing of the camera frame
            mWikitudeRenderExtension.onDrawFrame(unused);
        }

        for (TreeMap.Entry<String, Renderable> pairOccluder : mOccluders.entrySet()) {
            Renderable renderable = pairOccluder.getValue();
            renderable.onDrawFrame();
        }

        for (TreeMap.Entry<String, Renderable> pairRenderables : mRenderables.entrySet()) {
            Renderable renderable = pairRenderables.getValue();
            renderable.onDrawFrame();
        }
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onSurfaceCreated(unused, config);
        }

        for (TreeMap.Entry<String, Renderable> pairOccluder : mOccluders.entrySet()) {
            Renderable renderable = pairOccluder.getValue();
            renderable.onSurfaceCreated();
        }

        for (TreeMap.Entry<String, Renderable> pairRenderables : mRenderables.entrySet()) {
            Renderable renderable = pairRenderables.getValue();
            renderable.onSurfaceCreated();
        }
    }

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onSurfaceChanged(unused, width, height);
        }
    }

    public void onResume() {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onResume();
        }
    }

    public void onPause() {
        if (mWikitudeRenderExtension != null) {
            mWikitudeRenderExtension.onPause();
        }
    }

    public synchronized void setRenderablesForKey(final String key, final Renderable renderbale, final Renderable occluder) {
        if (occluder != null) {
            mOccluders.put(key, occluder);
        }

        mRenderables.put(key, renderbale);
    }

    public synchronized void removeRenderablesForKey(final String key) {
        mRenderables.remove(key);
        mOccluders.remove(key);
    }

    public synchronized void removeAllRenderables() {
        mRenderables.clear();
        mOccluders.clear();
    }

    public synchronized Renderable getRenderableForKey(final String key) {
        return mRenderables.get(key);
    }

    public synchronized Renderable getOccluderForKey(final String key) {
        return mOccluders.get(key);
    }
}
