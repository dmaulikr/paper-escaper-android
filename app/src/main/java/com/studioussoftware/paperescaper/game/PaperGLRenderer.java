package com.studioussoftware.paperescaper.game;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.studioussoftware.paperescaper.gameobjects.PaperSheet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLRenderer implements GLSurfaceView.Renderer {

    private final float[] ProjectionMatrix = new float[16];
    private final float[] ViewMatrix = new float[16];

    private PaperSheet sheet;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        sheet = new PaperSheet();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background oclor
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera's initial position
        Matrix.setLookAtM(ViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);

        sheet.draw(ProjectionMatrix, ViewMatrix);
    }


    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = width / (float) height;

        // This is applied to object coordinates in the onDrawFrame method
        Matrix.frustumM(ProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
