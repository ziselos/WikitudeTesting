package com.wikitude.samples.rendering.external;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class StrokedRectangle extends Renderable {

    public enum Type {
        FACE, STANDARD, EXTENDED, TRACKING_3D
    }

    private final static String TAG = "StrokedRectangle";

    String mFragmentShaderCode =
            "precision mediump float;" +
            "uniform vec3 Color;" +
            "void main()" +
            "{" +
            "  gl_FragColor = vec4(Color, 1.0);" +
            "}";

    String mVertexShaderCode =
            "attribute vec4 v_position;" +
            "uniform mat4 Projection;" +
            "uniform mat4 ModelView;" +
            "uniform mat4 Scale;" +
            "void main()" +
            "{" +
            "  gl_Position = Projection * ModelView * Scale * v_position;" +
            "}";

    private int mAugmentationProgram = -1;
    private int mPositionSlot = -1;
    private int mProjectionUniform = -1;
    private int mModelViewUniform = -1;
    private int mColorUniform = -1;
    private int mScaleUniform = -1;

    private float mRed = 1.0f;
    private float mGreen = 0.58f;
    private float mBlue = 0.16f;

    private float mXScale = 1.0f;
    private float mYScale = 1.0f;

    static float sRectVerts[] = {
            -0.5f, -0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
            0.5f,  0.5f, 0.0f,
            0.5f, -0.5f, 0.0f };

    static float sRectVertsExtended[] = {
            -0.7f, -0.7f, 0.0f,
            -0.7f,  0.7f, 0.0f,
            0.7f,  0.7f, 0.0f,
            0.7f, -0.7f, 0.0f };

    static float sRectVertsFace[] = {
            -0.5f, -0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
            0.5f,  0.5f, 0.0f,
            0.5f, -0.5f, 0.0f };


    private final ShortBuffer mIndicesBuffer;
    private final FloatBuffer mRectBuffer;

    private final short mIndices[] = { 0, 1, 2, 3 };

    public StrokedRectangle() {
        this(Type.STANDARD);
    }

    public StrokedRectangle(Type type) {
        ByteBuffer dlb = ByteBuffer.allocateDirect(mIndices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mIndicesBuffer = dlb.asShortBuffer();
        mIndicesBuffer.put(mIndices);
        mIndicesBuffer.position(0);

        ByteBuffer bb = ByteBuffer.allocateDirect(sRectVerts.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mRectBuffer = bb.asFloatBuffer();
        if (type == Type.EXTENDED) {
            mRectBuffer.put(sRectVertsExtended);
        } else if (type == Type.FACE || type == Type.TRACKING_3D) {
            mRectBuffer.put(sRectVertsFace);
        } else {
            mRectBuffer.put(sRectVerts);
        }
        mRectBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {
        compileShaders();
    }

    @Override
    public void onDrawFrame() {
        if (mAugmentationProgram == -1) {
            compileShaders();
        }

        if (this.projectionMatrix == null || this.viewMatrix == null) {
            return;
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(mAugmentationProgram);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glVertexAttribPointer(mPositionSlot, 3, GLES20.GL_FLOAT, false, 0, mRectBuffer);
        GLES20.glEnableVertexAttribArray(mPositionSlot);

        GLES20.glUniformMatrix4fv(mProjectionUniform, 1, false, this.projectionMatrix, 0);
        GLES20.glUniformMatrix4fv(mModelViewUniform, 1, false, this.viewMatrix, 0);

        GLES20.glUniform3f(mColorUniform, mRed, mGreen, mBlue);

        float[] scaleMatrix = {
                mXScale,    0.0f,       0.0f,       0.0f,
                0.0f,       mYScale,    0.0f,       0.0f,
                0.0f,       0.0f,       1.0f,       0.0f,
                0.0f,       0.0f,       0.0f,       1.0f
        };

        GLES20.glUniformMatrix4fv(mScaleUniform, 1, false, scaleMatrix, 0);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glLineWidth(10.0f);

        GLES20.glDrawElements(GLES20.GL_LINE_LOOP, mIndices.length, GLES20.GL_UNSIGNED_SHORT, mIndicesBuffer);

        GLES20.glLineWidth(1.0f);
    }

    public void setColor(float r, float g, float b) {
        mRed = r;
        mGreen = g;
        mBlue = b;
    }

    public float getXScale() {
        return mXScale;
    }

    public void setXScale(float xScale) {
        this.mXScale = xScale;
    }

    public float getYScale() {
        return mYScale;
    }

    public void setYScale(float yScale) {
        this.mYScale = yScale;
    }

    private void compileShaders() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);
        mAugmentationProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mAugmentationProgram, vertexShader);
        GLES20.glAttachShader(mAugmentationProgram, fragmentShader);
        GLES20.glLinkProgram(mAugmentationProgram);

        mPositionSlot = GLES20.glGetAttribLocation(mAugmentationProgram, "v_position");
        mModelViewUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "ModelView");
        mProjectionUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "Projection");
        mColorUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "Color");
        mScaleUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "Scale");
    }

    private static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
