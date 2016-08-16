package com.studioussoftware.paperescaper.game;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.IGameObject;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLRenderer implements GLSurfaceView.Renderer {

    private final float[] PerspectiveMatrix = new float[16];
    private final float[] ViewMatrix = new float[16];

    private PaperSheet sheet;   // TODO: Have object creation be done by the GameManager (attempts to do this have failed)

    public void updateCamera(Vector3 position, Vector3 forward, Vector3 up) {
        Matrix.setLookAtM(ViewMatrix, 0,
                position.x, position.y, position.z,
                position.x + forward.x, position.y + forward.y, position.z + forward.z,     // Expects lookAtPoint
                up.x, up.y, up.z);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        sheet = new PaperSheet();
        sheet.initGL();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background oclor
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        sheet.draw(PerspectiveMatrix, ViewMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = width / (float) height;

        Matrix.perspectiveM(PerspectiveMatrix, 0, 90, ratio, 0.01f, 10000f);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
