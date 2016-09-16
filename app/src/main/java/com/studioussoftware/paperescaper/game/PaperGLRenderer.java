package com.studioussoftware.paperescaper.game;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.ICameraToGL;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.studioussoftware.paperescaper.model.Difficulty;
import com.studioussoftware.paperescaper.model.Vector3;
import com.studioussoftware.paperescaper.views.PaperGLView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLRenderer implements GLSurfaceView.Renderer, ICameraToGL {

    private final float[] PerspectiveMatrix = new float[16];
    private final float[] ViewMatrix = new float[16];

    private GameManager manager;

    public PaperGLRenderer(PaperGLView view) {
        manager = new GameManager(this);
        view.setManager(manager);
    }

    public void setManager(IManager manager_) {
        manager = (GameManager) manager_;
    }

    /**
     * Takes the Camera vectors and converts them to an OpenGL matrix
     * @param position
     * @param forward
     * @param up
     */
    public void updateCamera(Vector3 position, Vector3 forward, Vector3 up) {
        Matrix.setLookAtM(ViewMatrix, 0,
                position.x, position.y, position.z,
                position.x + forward.x, position.y + forward.y, position.z + forward.z,     // Expects lookAtPoint
                up.x, up.y, up.z);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        PaperSheet.initGL();
        manager.updateDifficulty(Difficulty.MEDIUM);    // TODO: Move this elsewhere
        manager.initGame();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        manager.updateGame();
        manager.drawGame(PerspectiveMatrix, ViewMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = width / (float) height;

        Matrix.perspectiveM(PerspectiveMatrix, 0, 60, ratio, 0.01f, 10000f);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
