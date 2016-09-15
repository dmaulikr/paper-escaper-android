package com.studioussoftware.paperescaper.game;

import java.util.LinkedList;

import android.view.MotionEvent;

import com.studioussoftware.paperescaper.gameobjects.Camera;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.ILevelChangedListener;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.studioussoftware.paperescaper.model.Axis;
import com.studioussoftware.paperescaper.model.BoundsChecker;
import com.studioussoftware.paperescaper.model.Difficulty;
import com.studioussoftware.paperescaper.model.Vector3;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class GameManager implements IManager {

    private ILevelChangedListener levelChangedListener;
    private PaperGLRenderer renderer;
    private Camera camera;
    private BoundsChecker boundsChecker;

    private boolean gameInitialized = false;
    private boolean gameStarted = false;
    private boolean playerDead = false;
    private int level = 1;

    private float floorHeight = 1000;
    private float floorWidth = 1000;
    private final float floorBuffer = 20;   // Used for calculating player bounds

    private float previousX = 0;
    private float previousY = 0;

    private LinkedList<PaperSheet> sheets;
    private PaperSheet floor;

    public GameManager(PaperGLRenderer renderer_) {
        renderer = renderer_;

        updateFloorSize();
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

    /**
     * Handle dragging on the screen, which should rotate the camera
     * @param x
     * @param y
     */
    private void swipe(float x, float y) {
        float multiplier = 0.2f;
        camera.yaw(-x * multiplier);
        camera.pitch(-y * multiplier);
        updateCamera();
    }

    /**
     * Handle the player moving the Joystick to move the camera
     * @param angle in degrees
     * @param power [0 - 100]
     * @param direction unused
     */
    @Override
    public void onJoystickMove(int angle, int power, int direction) {
        camera.walk(-angle, power / 8.f, boundsChecker);
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
        floor = new PaperSheet(true);

        sheets = new LinkedList<>();
        for (int i = 0; i < 4; ++i) {
            sheets.add(new PaperSheet());
        }
        sheets.getLast().startRotating();
    }

    public void updateDifficulty(Difficulty level) {
        switch (level) {
            case EASY:
                floorHeight = 600;
                break;
            case MEDIUM:
                floorHeight = 1000;
                break;
            case HARD:
                floorHeight = 1500;
                break;
        }
        updateFloorSize();
    }

    private void updateFloorSize() {
        PaperSheet.setPaperSize(floorHeight);
        floorWidth = PaperSheet.getPaperWidth();
        boundsChecker = new BoundsChecker(floorWidth, floorHeight, floorBuffer);
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
