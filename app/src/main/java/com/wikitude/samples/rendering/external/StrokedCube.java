package com.wikitude.samples.rendering.external;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class StrokedCube extends Renderable{

    private final static String TAG = "StrokedCube";

    String mFragmentShaderCode =
        "precision mediump float;" +
        "void main()" +
        "{" +
        "  gl_FragColor = vec4(1.0, 0.58, 0.1, 1.0);" +
        "}";

    String mVertexShaderCode =
        "attribute vec4 v_position;" +
        "uniform mat4 u_projection;" +
        "uniform mat4 u_modelView;" +
        "uniform mat4 u_scale;" +
        "uniform mat4 u_translation;" +
        "void main()" +
        "{" +
        "  gl_Position = u_projection * u_modelView * u_translation * u_scale * v_position;" +
        "}";

    private int mAugmentationProgram = -1;
    private int mPositionSlot = -1;
    private int mProjectionUniform = -1;
    private int mModelViewUniform = -1;
    private int mScaleMatrixUniform = -1;
    private int mTranslateMatrixUniform = -1;

    private float mXScale = 1.0f;
    private float mYScale = 1.0f;
    private float mZScale = 1.0f;

    private float mXTranslate = 0.0f;
    private float mYTranslate = 0.0f;
    private float mZTranslate = 0.0f;

    static float sCubeVertices[] = {
        -0.5f, -0.5f,  0.5f,
        -0.5f,  0.5f,  0.5f,
         0.5f,  0.5f,  0.5f,
         0.5f, -0.5f,  0.5f,
         0.5f, -0.5f, -0.5f,
         0.5f,  0.5f, -0.5f,
         0.5f,  0.5f,  0.5f,
         0.5f,  0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
         0.5f, -0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        -0.5f,  0.5f, -0.5f,
        -0.5f,  0.5f,  0.5f,
        -0.5f, -0.5f,  0.5f,
         0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f,  0.5f,
        -0.5f, -0.5f, -0.5f
    };

    static short sCubeIndices[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

    private final ShortBuffer mIndicesBuffer;
    private final FloatBuffer mCubeBuffer;

    public StrokedCube() {
        ByteBuffer dlb = ByteBuffer.allocateDirect(sCubeIndices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mIndicesBuffer = dlb.asShortBuffer();
        mIndicesBuffer.put(sCubeIndices);
        mIndicesBuffer.position(0);

        ByteBuffer bb = ByteBuffer.allocateDirect(sCubeVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mCubeBuffer = bb.asFloatBuffer();
        mCubeBuffer.put(sCubeVertices);
        mCubeBuffer.position(0);
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

        GLES20.glUseProgram(mAugmentationProgram);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glVertexAttribPointer(mPositionSlot, 3, GLES20.GL_FLOAT, false, 0, mCubeBuffer);
        GLES20.glEnableVertexAttribArray(mPositionSlot);

        GLES20.glUniformMatrix4fv(mProjectionUniform, 1, false, this.projectionMatrix, 0);
        GLES20.glUniformMatrix4fv(mModelViewUniform, 1, false, this.viewMatrix, 0);

        float[] scaleMatrix = {
                mXScale,    0.0f,       0.0f,       0.0f,
                0.0f,       mYScale,    0.0f,       0.0f,
                0.0f,       0.0f,       mZScale,    0.0f,
                0.0f,       0.0f,       0.0f,       1.0f
        };

        float[] translateMatrix = {
                1.0f,               0.0f,               0.0f,               0.0f,
                0.0f,               1.0f,               0.0f,               0.0f,
                0.0f,               0.0f,               1.0f,               0.0f,
                mXTranslate,        mYTranslate,        mZTranslate,        1.0f
        };

        GLES20.glUniformMatrix4fv(mScaleMatrixUniform, 1, false, scaleMatrix, 0);
        GLES20.glUniformMatrix4fv(mTranslateMatrixUniform, 1, false, translateMatrix, 0);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glLineWidth(10.0f);
        GLES20.glDrawElements(GLES20.GL_LINE_LOOP, sCubeIndices.length, GLES20.GL_UNSIGNED_SHORT, mIndicesBuffer);
        GLES20.glLineWidth(1.0f);
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

    public float getZScale() {
        return mZScale;
    }

    public void setZScale(float zScale) {
        this.mZScale = zScale;
    }

    public float getXTranslate() {
        return mXTranslate;
    }

    public void setXTranslate(float xTranslate) { this.mXTranslate = xTranslate; }

    public float getYTranslate() {
        return mYTranslate;
    }

    public void setYTranslate(float yTranslate) { this.mYTranslate = yTranslate; }

    public float getZTranslate() {
        return mZTranslate;
    }

    public void setZTranslate(float zTranslate) { this.mZTranslate = zTranslate; }

    private void compileShaders() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);
        mAugmentationProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mAugmentationProgram, vertexShader);
        GLES20.glAttachShader(mAugmentationProgram, fragmentShader);
        GLES20.glLinkProgram(mAugmentationProgram);

        mPositionSlot = GLES20.glGetAttribLocation(mAugmentationProgram, "v_position");
        mModelViewUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "u_modelView");
        mProjectionUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "u_projection");
        mScaleMatrixUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "u_scale");
        mTranslateMatrixUniform = GLES20.glGetUniformLocation(mAugmentationProgram, "u_translation");
    }

    private static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
