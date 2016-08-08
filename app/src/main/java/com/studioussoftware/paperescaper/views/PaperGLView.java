package com.studioussoftware.paperescaper.views;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperGLView extends GLSurfaceView {

    private PaperGLRenderer renderer;

    public PaperGLView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new PaperGLRenderer();
        setRenderer(renderer);
    }
}
