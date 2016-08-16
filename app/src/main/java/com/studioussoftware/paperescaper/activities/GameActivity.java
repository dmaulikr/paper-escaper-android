package com.studioussoftware.paperescaper.activities;

import android.app.Activity;
import android.os.Bundle;

import com.studioussoftware.paperescaper.game.GameManager;
import com.studioussoftware.paperescaper.views.PaperGLView;

public class GameActivity extends Activity {

    private PaperGLView openGL;
    private GameManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openGL = new PaperGLView(this);
        setContentView(openGL);

        manager = new GameManager(this, openGL);
    }
}
