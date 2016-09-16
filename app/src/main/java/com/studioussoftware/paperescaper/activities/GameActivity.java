package com.studioussoftware.paperescaper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.ILevelChangedListener;
import com.studioussoftware.paperescaper.views.PaperGLView;
import com.zerokol.views.JoystickView;

public class GameActivity extends Activity implements ILevelChangedListener {

    private PaperGLView glView;
    private String levelPrefix;
    private TextView levelBox;

    private boolean resumePromptOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_layout);

        levelPrefix = getResources().getString(R.string.level_prefix);
        levelBox = (TextView) findViewById(R.id.levelBox);
        updateLevel(1);

        glView = (PaperGLView) findViewById(R.id.glView);
        glView.setLevelChangedListener(this);

        if (savedInstanceState != null) {
            // Resuming previously loaded game, pause and show dialog allowing them to resume
            glView.loadFromSavedInstanceState(savedInstanceState);

            resumePromptOpen = true;
            glView.pause();
            new AlertDialog.Builder(this)
                .setTitle("Game Paused")
                .setMessage("Press OK to resume your game")
                .setPositiveButton("Resume", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        glView.unpause();
                        resumePromptOpen = false;
                    }
                }).create().show();

        }

        long joystickLoopInterval = 10;  // ms
        JoystickView joystick = (JoystickView) findViewById(R.id.joystick);
        joystick.setOnJoystickMoveListener(glView, joystickLoopInterval);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        glView.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        glView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Don't resume while any dialogs are open
        if (!resumePromptOpen) {
            glView.unpause();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // Some OpenGL code isn't serializable, so reinitialize next time onCreate() is called
        PaperSheet.resetGL();   // TODO: Think of a less expensive way to handle this
        super.onDestroy();
    }

    public void onLevelChanged(final int level) {
        // This should be called from the OpenGL thread, but need to do to this on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateLevel(level);
            }
        });
    }

    private void updateLevel(int level) {
        levelBox.setText(levelPrefix + " " + level);
    }
}
