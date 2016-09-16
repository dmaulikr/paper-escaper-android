package com.studioussoftware.paperescaper.interfaces;

import java.io.Serializable;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public interface IGameObject extends Serializable {
    void update();
    void draw(float[] perspectiveMatrix, float[] viewMatrix);
}
