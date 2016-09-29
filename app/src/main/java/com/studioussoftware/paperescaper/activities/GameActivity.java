package com.studioussoftware.paperescaper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.IGuiUpdater;
import com.studioussoftware.paperescaper.model.Constants;
import com.studioussoftware.paperescaper.views.PaperGLView;
import com.zerokol.views.JoystickView;

public class GameActivity extends Activity implements IGuiUpdater {

    private PaperGLView glView;
    private String levelPrefix;
    private TextView levelBox;
    private TextView debugBox;

    private boolean resumePromptOpen = false;
    private boolean restartPromptOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_layout);

        levelPrefix = getResources().getString(R.string.level_prefix);
        levelBox = (TextView) findViewById(R.id.levelBox);
        updateLevel(1);

        debugBox = (TextView) findViewById(R.id.debugBox);
        if (Constants.DEBUG_MODE) {
            debugBox.setVisibility(View.VISIBLE);
        }

        glView = (PaperGLView) findViewById(R.id.glView);
        glView.setGuiUpdater(this);

        if (savedInstanceState != null) {
            // Resuming previously loaded game, pause and show dialog allowing them to resume
            glView.loadFromSavedInstanceState(savedInstanceState);

            resumePromptOpen = true;
            glView.pause();
            new AlertDialog.Builder(this)
                .setTitle("Game Paused")    // TODO: Load these strings
                .setMessage("Press to resume to your game")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
        if (!resumePromptOpen && !restartPromptOpen) {
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

    /**
     * IGuiUpdater functions are called from the OpenGL thread, but the GUI updating needs to be done on the UI thread
     */

    @Override
    public void onLevelChanged(final int level) {
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

    @Override
    public void receiveDebugInfo(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                debugBox.setText(info);
            }
        });
    }

    @Override
    public void gameEnded() {
        // If game is minimized while this prompt is open, then when onResume is called it will
        // unpause the game, even if the prompt is still open. This prevents that
        final Context context = this;
        restartPromptOpen = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                    .setTitle("Game Over")
                    .setMessage("Press to restart the game")
                    .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            glView.restart();
                            restartPromptOpen = false;
                        }
                    }).create().show();
            }
        });
    }
}
