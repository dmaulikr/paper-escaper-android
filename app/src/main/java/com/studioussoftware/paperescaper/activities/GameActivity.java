package com.studioussoftware.paperescaper.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.studioussoftware.paperescaper.R;
import com.studioussoftware.paperescaper.interfaces.ILevelChangedListener;
import com.studioussoftware.paperescaper.views.PaperGLView;
import com.zerokol.views.JoystickView;

public class GameActivity extends Activity implements ILevelChangedListener {

    String levelPrefix;
    TextView levelBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        levelPrefix = getResources().getString(R.string.level_prefix);

        setContentView(R.layout.game_layout);
        PaperGLView glView = (PaperGLView) findViewById(R.id.glView);
        glView.setLevelChangedListener(this);

        levelBox = (TextView) findViewById(R.id.levelBox);
        updateLevel(1);

        long joystickLoopInterval = 10;  // ms
        JoystickView joystick = (JoystickView) findViewById(R.id.joystick);
        joystick.setOnJoystickMoveListener(glView, joystickLoopInterval);
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
