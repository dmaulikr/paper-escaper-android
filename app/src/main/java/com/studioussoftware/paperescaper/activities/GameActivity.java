package com.studioussoftware.paperescaper.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.database.StoreOfflineHighScore;
import com.studioussoftware.paperescaper.database.StoreOnlineHighScore;
import com.studioussoftware.paperescaper.gameobjects.PaperSheet;
import com.studioussoftware.paperescaper.interfaces.IGuiUpdater;
import com.studioussoftware.paperescaper.interfaces.IScoreStoredListener;
import com.studioussoftware.paperescaper.model.Constants;
import com.studioussoftware.paperescaper.views.DialogFactory;
import com.studioussoftware.paperescaper.views.PaperGLView;
import com.zerokol.views.JoystickView;

public class GameActivity extends Activity implements IGuiUpdater, OnCheckedChangeListener, IScoreStoredListener {

    private PaperGLView glView;
    private TextView levelBox;
    private TextView debugBox;

    private boolean resumedOnce = false;    // Check if onResume called at least once
    private boolean resumePromptOpen = false;
    private boolean highscorePromptOpen = false;
    private final String HIGHSCORE_PROMPT_KEY = "HighScorePromptOpen";
    private boolean saveHighScore = true;
    private int scoresToStore = 0;

    private boolean gameOver = false;
    private final int STARTING_LEVEL = 1;
    private int currentLevel = STARTING_LEVEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.game_layout);

        levelBox = (TextView) findViewById(R.id.levelBox);
        updateLevel(STARTING_LEVEL);

        debugBox = (TextView) findViewById(R.id.debugBox);
        if (Constants.DEBUG_MODE) {
            debugBox.setVisibility(View.VISIBLE);
        }

        glView = (PaperGLView) findViewById(R.id.glView);
        glView.setGuiUpdater(this);

        if (savedInstanceState != null) {
            // Resuming previously loaded game, pause and show dialog allowing them to resume (unless highscore prompt was open)
            glView.loadFromSavedInstanceState(savedInstanceState);
            highscorePromptOpen = savedInstanceState.getBoolean(HIGHSCORE_PROMPT_KEY);

            if (!highscorePromptOpen) {
                // Even if Resume dialog was already open, onDestroy closed it so reopen it
                showResumeDialog();
            }
            else {
                // Reopen the highscore prompt
                onBackPressed();
            }
        }

        long joystickLoopInterval = 10;  // ms
        JoystickView joystick = (JoystickView) findViewById(R.id.joystick);
        joystick.setOnJoystickMoveListener(glView, joystickLoopInterval);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        glView.saveInstanceState(outState);
        outState.putBoolean(HIGHSCORE_PROMPT_KEY, highscorePromptOpen);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        glView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // Android has Activities call onResume once at startup, don't show resume dialog when that happens
        if (!resumedOnce) {
            resumedOnce = true;
        }
        // If no dialogs open, prompt the user to resume
        else if (!resumePromptOpen && !highscorePromptOpen) {
            showResumeDialog();
        }
        super.onResume();
    }

    private void showResumeDialog() {
        resumePromptOpen = true;
        glView.pause();
        new AlertDialog.Builder(this)
                .setTitle(R.string.paused_dialog_title)
                .setMessage(R.string.paused_dialog_message)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        resumeClicked();
                    }
                })
                .setPositiveButton(R.string.paused_dialog_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resumeClicked();
                    }
                }).create().show();
    }

    private void resumeClicked() {
        glView.unpause();
        resumePromptOpen = false;
    }

    @Override
    protected void onDestroy() {
        // Some OpenGL code isn't serializable, so reinitialize next time onCreate() is called
        PaperSheet.resetGL();   // TODO: Think of a less expensive way to handle this
        super.onDestroy();
    }

    /**
     * Confirm with the player if they would like to quit
     * If they select that they want to store their current score, prompt them for their name
     */
    @Override
    public void onBackPressed() {
        glView.pause();

        // If game is minimized while this prompt is open, then when onResume is called it will
        // unpause the game, even if the prompt is still open. This prevents that
        highscorePromptOpen = true;

        // Save score button listeners
        final OnClickListener onPositiveScore = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeHighScore(dialog);
            }
        };

        final OnClickListener onNegativeScore = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doneStoringScores();
            }
        };

        // Ask if they want to quit if the game isn't over
        if (!gameOver) {
            CheckBox checkbox = (CheckBox) View.inflate(this, R.layout.checkbox, null);
            checkbox.setText(R.string.quit_dialog_checkbox);
            checkbox.setOnCheckedChangeListener(this);

            // Create AlertDialog
            new AlertDialog.Builder(this)
            .setTitle(R.string.quit_dialog_title)
            .setMessage(R.string.quit_dialog_message)
            .setView(checkbox)
            .setPositiveButton(R.string.quit_dialog_yes, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Quit the game, but first see if should save high score
                    if (saveHighScore && currentLevel > STARTING_LEVEL) {
                        DialogFactory.showSaveHighscoreDialog(GameActivity.this, onPositiveScore, onNegativeScore);
                    }
                    else {
                        doneStoringScores();
                    }
                }
            })
            .setNegativeButton(R.string.quit_dialog_no, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    saveHighScore = true;
                    highscorePromptOpen = false;
                    glView.unpause();
                }
            })
            .create().show();
        }
        // Don't ask if they want to quit, just ask for highscore information
        else {
            if (currentLevel > STARTING_LEVEL) {
                DialogFactory.showSaveHighscoreDialog(GameActivity.this, onPositiveScore, onNegativeScore);
            }
            else {
                // They failed to beat even level one
                new AlertDialog.Builder(this)
                .setTitle(R.string.gameover_dialog_title)
                .setMessage(R.string.gameover_dialog_message)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        doneStoringScores();
                    }
                })
                .setPositiveButton(R.string.gameover_dialog_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doneStoringScores();
                    }
                }).create().show();
            }
        }
    }

    /**
     * Using the information from the dialog, store the score offline,
     * and if they indicated, online as well
     * @param dialog
     */
    private void storeHighScore(DialogInterface dialog) {
        AlertDialog d = (AlertDialog) dialog;
        CheckBox checkbox = (CheckBox) d.findViewById(R.id.checkbox_for_dialog);
        EditText editText = (EditText) d.findViewById(R.id.edit_text);

        String name = editText.getText().toString();

        // Store offline
        ++scoresToStore;
        new StoreOfflineHighScore(this, this, name, currentLevel).execute();

        // Store online
        if (checkbox.isChecked()) {
            ++scoresToStore;
            new StoreOnlineHighScore(this, this, name, currentLevel).execute();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        saveHighScore = isChecked;
    }

    @Override
    public void scoreStored() {
        if (--scoresToStore == 0) {
            doneStoringScores();
        }
    }

    private void doneStoringScores() {
        super.onBackPressed();
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
        levelBox.setText(getString(R.string.current_level, level));
        currentLevel = level;
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
        gameOver = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        });
    }
}
