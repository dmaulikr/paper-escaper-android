package com.studioussoftware.paperescaper.game;

import java.util.LinkedList;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.studioussoftware.paperescaper.gameobjects.Camera;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.IGameObject;
import com.studioussoftware.paperescaper.interfaces.IManager;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class GameManager implements IManager, ScaleGestureDetector.OnScaleGestureListener {

    private ScaleGestureDetector scaleDetector;
    private PaperGLRenderer renderer;
    private Camera camera;

    private boolean gameInitialized = false;
    private boolean gameStarted = false;
    private boolean playerDead = false;

    private float previousScale = 0;
    private float previousX = 0;
    private float previousY = 0;

    private LinkedList<PaperSheet> sheets;

    public GameManager(Context context, PaperGLRenderer renderer_) {
        scaleDetector = new ScaleGestureDetector(context, this);

        renderer = renderer_;
    }

    public void initGame() {
        if (!gameInitialized) {
            camera = new Camera(new Vector3(0, 0, 3000),
                    new Vector3(0, 0, -1),
                    new Vector3(0, 1, 0));
            updateCamera();

            createInitialSheets();
            gameInitialized = true;
        }
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

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

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) { return true; }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float deltaScale = previousScale - detector.getScaleFactor();
        scale(deltaScale);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {}

    private void swipe(float x, float y) {
        float multiplier = 0.1f;
        camera.yaw(-x * multiplier);
        camera.pitch(-y * multiplier);
        updateCamera();
    }

    private void scale(float deltaScale) {
        // Move along the forward vector
        // TODO: Fix this
        /*
        Vector3 position = camera.getPosition();
        Vector3 forward = camera.getForward().normalized();
        float scaleAmount = 5;
        float scale = (deltaScale > 0) ? scaleAmount : -scaleAmount;
        position = position.added(forward.scaled(scale));
        camera.setPosition(position);
        updateCamera();
        */
    }

    private void updateCamera() {
        renderer.updateCamera(camera.getPosition(), camera.getForward(), camera.getUp());
    }

    private void createInitialSheets() {
        sheets = new LinkedList<>();
        for (int i = 0; i < 4; ++i) {
            sheets.add(new PaperSheet());
        }
        sheets.getLast().startRotating();
    }

    public void updateGame() {
        if (!gameStarted) {
            Vector3 position = camera.getPosition();
            // if timer == 2000
            gameStarted = true;
            position.y = 100;
            camera.setPosition(position);
            // else
        }
        if (gameStarted && !playerDead) {
            for (PaperSheet sheet : sheets) {
                sheet.update();
            }
            if (sheets.getLast().getShouldDelete()) {
                sheets.removeLast();
                sheets.addFirst(new PaperSheet());
                sheets.getLast().startRotating();
            }
        }
    }

    public void drawGame(float[] perspectiveMatrix, float[] vMatrix) {
        for (PaperSheet sheet : sheets) {
            sheet.draw(perspectiveMatrix, vMatrix);
        }
    }
}
