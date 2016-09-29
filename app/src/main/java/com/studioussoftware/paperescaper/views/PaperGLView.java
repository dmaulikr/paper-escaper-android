package com.studioussoftware.paperescaper.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;
import com.studioussoftware.paperescaper.interfaces.IGuiUpdater;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.zerokol.views.JoystickView;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLView extends GLSurfaceView implements JoystickView.OnJoystickMoveListener {

    private final String MANAGER_KEY = "Manager";
    private IManager manager = null;
    private PaperGLRenderer renderer;
    private IGuiUpdater guiUpdater = null;

    public PaperGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        construct();
    }

    public PaperGLView(Context context) {
        super(context);
        construct();
    }

    private void construct() {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new PaperGLRenderer(this);
        setRenderer(renderer);
    }

    /**
     * Meant to go to the manager, either now or later
     * @param listener
     */
    public void setGuiUpdater(IGuiUpdater listener) {
        guiUpdater = listener;
        if (manager != null) {
            manager.setGuiUpdater(listener);
        }
    }

    public void setManager(IManager obj) {
        manager = obj;
        if (manager != null) {
            manager.setGuiUpdater(guiUpdater);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Send to the manager to deal with
        if (manager == null) {
            return super.onTouchEvent(event);
        }
        return manager.handleTouchEvent(event);
    }

    @Override
    public void onValueChanged(int angle, int power, int direction) {
        if (manager != null) {
            manager.onJoystickMove(angle, power, direction);
        }
    }

    /**
     * When game closing temporarily, save the Manager for later
     * @param outState
     */
    public void saveInstanceState(Bundle outState) {
        outState.putSerializable(MANAGER_KEY, manager);
    }

    /**
     * When game reloaded, update all the pointers to use the saved Manager class
     * @param inState
     */
    public void loadFromSavedInstanceState(Bundle inState) {
        setManager((IManager)inState.get(MANAGER_KEY));
        renderer.setManager(manager);
        manager.setCameraToGLHandler(renderer);
    }

    public void pause() {
        manager.pause();
    }

    public void unpause() {
        manager.unpause();
    }

    public void restart() { manager.restart(); }
}
