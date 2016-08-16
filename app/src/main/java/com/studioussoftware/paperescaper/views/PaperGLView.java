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

    private IManager manager;
    private PaperGLRenderer renderer;

    public PaperGLView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new PaperGLRenderer();
        setRenderer(renderer);
    }

    public PaperGLRenderer getRenderer() {
        return renderer;
    }

    public void setManager(IManager obj) {
        manager = obj;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Send to the manager to deal with
        return manager.handleTouchEvent(event);
    }
}
