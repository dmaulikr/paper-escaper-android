package com.studioussoftware.paperescaper.interfaces;

/**
 * Created by Robbie Wolfe on 9/13/2016.
 */
public interface IGuiUpdater {
    void onLevelChanged(int newLevel);
    void receiveDebugInfo(String info);
    void gameEnded();
}
