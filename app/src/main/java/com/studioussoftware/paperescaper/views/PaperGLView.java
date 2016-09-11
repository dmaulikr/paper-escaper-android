package com.studioussoftware.paperescaper.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;
import com.studioussoftware.paperescaper.interfaces.IManager;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLView extends GLSurfaceView {

    private IManager manager = null;

    public PaperGLView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        setRenderer(new PaperGLRenderer(this));
    }

    public void setManager(IManager obj) {
        manager = obj;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Send to the manager to deal with
        if (manager == null) {
            return super.onTouchEvent(event);
        }
        return manager.handleTouchEvent(event);
    }
}
