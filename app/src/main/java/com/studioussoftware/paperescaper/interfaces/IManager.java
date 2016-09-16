package com.studioussoftware.paperescaper.interfaces;

import android.view.MotionEvent;

import java.io.Serializable;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public interface IManager extends Serializable {
    boolean handleTouchEvent(MotionEvent event);
    void pause();
    void unpause();
    void setCameraToGLHandler(ICameraToGL handler);
    void onJoystickMove(int angle, int power, int direction);
    void setLevelChangedListener(ILevelChangedListener listener);
}
