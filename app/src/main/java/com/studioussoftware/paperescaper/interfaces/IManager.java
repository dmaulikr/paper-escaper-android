package com.studioussoftware.paperescaper.interfaces;

import android.view.MotionEvent;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public interface IManager {
    boolean handleTouchEvent(MotionEvent event);
    void onJoystickMove(int angle, int power, int direction);
    void setLevelChangedListener(ILevelChangedListener listener);
}
