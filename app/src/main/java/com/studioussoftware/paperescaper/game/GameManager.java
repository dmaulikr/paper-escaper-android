package com.studioussoftware.paperescaper.game;

import java.util.LinkedList;

import android.content.Context;
import android.view.MotionEvent;

import com.studioussoftware.paperescaper.gameobjects.Camera;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.ILevelChangedListener;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.studioussoftware.paperescaper.model.Axis;
import com.studioussoftware.paperescaper.model.Vector3;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class GameManager implements IManager {

    private ILevelChangedListener levelChangedListener;
    private PaperGLRenderer renderer;
    private Camera camera;

    private boolean gameInitialized = false;
    private boolean gameStarted = false;
    private boolean playerDead = false;
    private int level = 1;

    private float previousScale = 0;
    private float previousX = 0;
    private float previousY = 0;

    private LinkedList<PaperSheet> sheets;
    private PaperSheet floor;

    public GameManager(PaperGLRenderer renderer_) {
        renderer = renderer_;
    }

    public void initGame() {
        if (!gameInitialized) {
            camera = new Camera(new Vector3(400, 1300, 0),
                    new Vector3(0, 0, -1),
                    new Vector3(0, 1, 0));
            updateCamera();

            createInitialSheets();
            gameInitialized = true;
        }
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;
                swipe(dx, dy);
                break;
        }
        previousX = x;
        previousY = y;
        return true;
    }

    private void swipe(float x, float y) {
        float multiplier = 0.2f;
        camera.yaw(-x * multiplier);
        camera.pitch(-y * multiplier);
        updateCamera();
    }

    @Override
    public void onJoystickMove(int angle, int power, int direction) {
        camera.walk(-angle, power / 8.f);
        updateCamera();
    }

    public void setLevelChangedListener(ILevelChangedListener listener) {
        levelChangedListener = listener;
    }

    private void updateCamera() {
        renderer.updateCamera(camera.getPosition(), camera.getForward(), camera.getUp());
    }

    private void createInitialSheets() {
        // Create the floor
        floor = new PaperSheet(false);
        floor.setFloor();

        sheets = new LinkedList<>();
        for (int i = 0; i < 4; ++i) {
            sheets.add(new PaperSheet());
        }
        sheets.getLast().startRotating();
    }

    public void updateGame() {
        if (!gameStarted) {
            // if timer == 2000
            gameStarted = true;
            camera.setPosition(Axis.Y, 100);
            updateCamera();
            // else
        }
        if (gameStarted && !playerDead) {
            for (PaperSheet sheet : sheets) {
                sheet.update();
            }
            if (sheets.getLast().getShouldDelete()) {
                ++level;
                if (levelChangedListener != null) {
                    levelChangedListener.onLevelChanged(level);
                }
                sheets.removeLast();
                sheets.addFirst(new PaperSheet());
                sheets.getLast().startRotating();
            }
        }
    }

    public void drawGame(float[] perspectiveMatrix, float[] vMatrix) {
        floor.draw(perspectiveMatrix, vMatrix);
        for (PaperSheet sheet : sheets) {
            sheet.draw(perspectiveMatrix, vMatrix);
        }
    }
}
