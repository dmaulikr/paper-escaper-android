package com.studioussoftware.paperescaper.game;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.studioussoftware.paperescaper.gameobjects.Camera;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.studioussoftware.paperescaper.views.PaperGLView;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class GameManager implements IManager, ScaleGestureDetector.OnScaleGestureListener {

    private ScaleGestureDetector scaleDetector;
    private PaperGLRenderer renderer;
    private Camera camera;

    private float previousScale = 0;
    private float previousX = 0;
    private float previousY = 0;

    public GameManager(Context context, PaperGLView glView) {
        camera = new Camera(new Vector3(0, 0, 3000),
                            new Vector3(0, 0, -1),
                            new Vector3(0, 1, 0));

        scaleDetector = new ScaleGestureDetector(context, this);

        glView.setManager(this);
        renderer = glView.getRenderer();
        updateCamera();
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
        camera.yaw(x * multiplier);
        camera.pitch(y * multiplier);
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
}
