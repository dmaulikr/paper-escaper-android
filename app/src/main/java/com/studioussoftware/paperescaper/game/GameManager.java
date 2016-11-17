package com.studioussoftware.paperescaper.game;

import java.util.LinkedList;

import android.view.MotionEvent;

import com.studioussoftware.paperescaper.gameobjects.Camera;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.ICameraToGL;
import com.studioussoftware.paperescaper.interfaces.IGuiUpdater;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.studioussoftware.paperescaper.model.Axis;
import com.studioussoftware.paperescaper.model.BoundsChecker;
import com.studioussoftware.paperescaper.model.Constants;
import com.studioussoftware.paperescaper.model.Difficulty;
import com.studioussoftware.paperescaper.model.Vector3;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class GameManager implements IManager {

    private static final long serialVersionUID = 1262935185132526255L;

    // These classes aren't Serializable, they'll be passed in anew when game recreated
    private transient IGuiUpdater guiUpdater;
    private transient ICameraToGL cameraToGLHandler;

    private static final Vector3 CAMERA_FORWARD = new Vector3(0, 0, -1);
    private static final Vector3 CAMERA_UP = new Vector3(0, 1, 0);
    private Camera camera;
    private BoundsChecker boundsChecker;

    private static final float MIN_PLAYER_HEIGHT = 5.f;
    private static final float PLAYER_INITIAL_X = 400f;
    private static final float PLAYER_INITIAL_HEIGHT = 1300.f;
    private static final float PLAYER_INITIAL_Z = 0;
    private static final float PLAYER_GAME_HEIGHT = 100.f;
    private static final float PLAYER_CRUSHING_SPEED = 5.f;
    private static final int GAME_START_TIMER_MAX = 20;//2000;
    private static final int NUM_STARTING_SHEETS = 4;   // Created all with the same hole size

    private static float shrinkingAmplitude;    // Hole shrinking variables, used for Exponential function, see below
    private static float shrinkingMeanLife;
    private static float fallingAmplitude;      // Pages falling speed variables
    private static float fallingMeanLife;

    private int gameStartTimer = 0;
    private boolean gameInitialized = false;
    private boolean gameStarted = false;
    private boolean paused = false;
    private boolean playerDead = false;
    private int level = 1;

    private float floorHeight = 1000;
    private float floorWidth = 1000;
    private final float floorBuffer = 20;   // Used for calculating player bounds

    private float previousX = 0;
    private float previousY = 0;

    private LinkedList<PaperSheet> sheets;
    private PaperSheet floor;

    public GameManager(ICameraToGL cameraToGL_) {
        cameraToGLHandler = cameraToGL_;

        // Calculate hole shrinking constants, see Exponential function below
        float levelOne = 1;         float valueOne = PaperSheet.MAX_HOLE_SIZE;      // Go from big to small
        float levelTwo = 80;        float valueTwo = PaperSheet.MIN_HOLE_SIZE;      // On level levelTwo + NUM_STARTING_SHEETS the hole size should be valueTwo
        shrinkingMeanLife   = (levelTwo - levelOne) / (float) Math.log(valueOne / valueTwo);
        shrinkingAmplitude  = valueOne / (float) (Math.exp(-levelOne / shrinkingMeanLife));

        // Calculate page falling speed constants,
        levelOne = 1;       valueOne = PaperSheet.MIN_ROTATION_SPEED;       // Start off slow and speed up
        levelTwo = 160;     valueTwo = PaperSheet.MAX_ROTATION_SPEED;
        fallingMeanLife = (levelTwo - levelOne) / (float) Math.log(valueOne / valueTwo);
        fallingAmplitude = valueOne / (float) (Math.exp(-levelOne / fallingMeanLife));

        updateFloorSize();
    }

    public void initGame() {
        if (!gameInitialized) {
            camera = new Camera(new Vector3(PLAYER_INITIAL_X, PLAYER_INITIAL_HEIGHT, PLAYER_INITIAL_Z), CAMERA_FORWARD, CAMERA_UP);
            updateCamera();

            createInitialSheets();
            gameInitialized = true;
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void unpause() {
        paused = false;
    }

    @Override
    public void restart() {
        playerDead = false;
        gameStarted = true;
        camera = new Camera(new Vector3(PLAYER_INITIAL_X, PLAYER_GAME_HEIGHT, PLAYER_INITIAL_Z), CAMERA_FORWARD, CAMERA_UP);
        updateCamera();
        level = 1;
        createInitialSheets();  // Will remove all the old ones as well
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
        if (!playerDead) {
            float multiplier = 0.2f;
            camera.yaw(-x * multiplier);
            camera.pitch(-y * multiplier);
            updateCamera();
        }
    }

    /**
     * Handle the player moving the Joystick to move the camera
     * @param angle in degrees
     * @param power [0 - 100]
     * @param direction unused
     */
    @Override
    public void onJoystickMove(int angle, int power, int direction) {
        if (!playerDead) {
            camera.walk(-angle, power / 8.f, boundsChecker);
            updateCamera();
        }
    }

    @Override
    public void setGuiUpdater(IGuiUpdater listener) {
        guiUpdater = listener;

        if (guiUpdater != null) {
            guiUpdater.onLevelChanged(level);
        }
    }

    @Override
    public void setCameraToGLHandler(ICameraToGL handler) {
        cameraToGLHandler = handler;
        updateCamera();
    }

    private void updateCamera() {
        cameraToGLHandler.updateCamera(camera.getPosition(), camera.getForward(), camera.getUp());
    }

    private void createInitialSheets() {
        // Create the floor
        floor = new PaperSheet(true);

        sheets = new LinkedList<>();
        for (int i = 0; i < NUM_STARTING_SHEETS; ++i) {
            sheets.add(new PaperSheet(getAHoleSize()));
        }
        sheets.getLast().startRotating();
    }

    /**
     * Depends on the current level
     * @return
     */
    private float getAHoleSize() {
        return Math.max(getExponentialFormulaValue(shrinkingAmplitude, shrinkingMeanLife, level), PaperSheet.MIN_HOLE_SIZE);
    }

    /**
     * Depends on the current level
     * @return
     */
    private float getFallingSpeed() {
        return Math.min(getExponentialFormulaValue(fallingAmplitude, fallingMeanLife, level), PaperSheet.MAX_ROTATION_SPEED);
    }

    /**
     * Formula for Exponential decay (and apparently growth) is y = A*e^(-t / M)
     * @param amplitude is A
     * @param meanLife is M, which has the formula (t2 - t2) / ln(y1 / y2)
     * @param t is time value
     * @return
     */
    private float getExponentialFormulaValue(float amplitude, float meanLife, float t) {
        return amplitude * (float) Math.exp(-t / meanLife);
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
        if (paused) {
            return;
        }
        if (!gameStarted) {
            // Start the game (unless was previously started, then restart() will need to be called to play again)
            if (gameStartTimer == GAME_START_TIMER_MAX) {
                gameStarted = true;
                camera.setPosition(Axis.Y, PLAYER_GAME_HEIGHT);
                updateCamera();
                gameStartTimer = GAME_START_TIMER_MAX + 1;
            }
            else if (gameStartTimer < GAME_START_TIMER_MAX) {
                ++gameStartTimer;
            }
        }
        else {
            // Collision detection
            Vector3 playerPosition = camera.getPosition();
            if (playerDead || sheets.getLast().isColliding(playerPosition.x, playerPosition.y, playerPosition.z)) {
                // End the game
                playerDead = true;  // In case it wasn't true already

                if (playerPosition.y > MIN_PLAYER_HEIGHT) {
                    // Crush the player
                    camera.setPosition(Axis.Y, playerPosition.y - PLAYER_CRUSHING_SPEED);
                    updateCamera();
                }
                else {
                    // Player fully crushed
                    gameStarted = false;
                    guiUpdater.gameEnded();
                }
            }

            // Update all the game objects
            if (!playerDead) {
                for (PaperSheet sheet : sheets) {
                    sheet.update();
                }
                if (sheets.getLast().getShouldDelete()) {
                    ++level;
                    if (guiUpdater != null) {
                        guiUpdater.onLevelChanged(level);
                    }
                    sheets.removeLast();
                    sheets.addFirst(new PaperSheet(getAHoleSize()));
                    sheets.getLast().startRotating();
                    PaperSheet.setFallingSpeed(getFallingSpeed());
                }

                if (Constants.CHEAT_MODE) {
                    // Move automatically towards the hole
                    Vector3 holeLocation = sheets.getLast().getHoleWorldLocation();     // X, Y, should be X, Z
                    holeLocation.z = holeLocation.y;
                    holeLocation.y = PLAYER_GAME_HEIGHT;        // To make no change in Y
                    camera.walkTowards(holeLocation, 0.1f);
                    updateCamera();
                }

                if (Constants.DEBUG_MODE) {
                    if (guiUpdater != null) {
                        guiUpdater.receiveDebugInfo("Inside hole? " + sheets.getLast().isInHole(playerPosition.x, playerPosition.z));
                    }
                }
            }
        }
    }

    public void drawGame(float[] perspectiveMatrix, float[] vMatrix) {
        if (!paused) {
            floor.draw(perspectiveMatrix, vMatrix);
            for (PaperSheet sheet : sheets) {
                sheet.draw(perspectiveMatrix, vMatrix);
            }
        }
    }
}
