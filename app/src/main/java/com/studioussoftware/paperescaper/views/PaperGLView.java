package com.studioussoftware.paperescaper.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;
import com.studioussoftware.paperescaper.interfaces.ILevelChangedListener;
import com.studioussoftware.paperescaper.interfaces.IManager;
import com.zerokol.views.JoystickView;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLView extends GLSurfaceView implements JoystickView.OnJoystickMoveListener {

    private IManager manager = null;
    private ILevelChangedListener levelChangedListener = null;

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

        setRenderer(new PaperGLRenderer(this));
    }

    /**
     * Meant to go to the manager, either now or later
     * @param listener
     */
    public void setLevelChangedListener(ILevelChangedListener listener) {
        levelChangedListener = listener;
        if (manager != null) {
            manager.setLevelChangedListener(listener);
        }
    }

    public void setManager(IManager obj) {
        manager = obj;
        manager.setLevelChangedListener(levelChangedListener);
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
}
