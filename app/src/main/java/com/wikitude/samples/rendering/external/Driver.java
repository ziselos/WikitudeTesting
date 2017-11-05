package com.wikitude.samples.rendering.external;

import java.util.Timer;
import java.util.TimerTask;

public class Driver {

    private final CustomSurfaceView mCustomSurfaceView;
    private final int mFps;
    private Timer mRenderTimer = null;


    public Driver(final CustomSurfaceView customSurfaceView, int fps) {
        mCustomSurfaceView = customSurfaceView;
        mFps = fps;

    }

    public void start() {
        if (mRenderTimer != null) {
            mRenderTimer.cancel();
        }

        mRenderTimer = new Timer();
        mRenderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mCustomSurfaceView.requestRender();
            }
        }, 0, 1000 / mFps);
    }

    public void stop() {
        mRenderTimer.cancel();
        mRenderTimer = null;
    }

}
