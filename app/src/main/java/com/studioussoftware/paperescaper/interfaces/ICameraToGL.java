package com.studioussoftware.paperescaper.interfaces;

import com.studioussoftware.paperescaper.model.Vector3;

/**
 * Created by Robbie Wolfe on 9/16/2016.
 */
public interface ICameraToGL {
    // Should take the Camera vectors and converts them to a OpenGL matrix
    void updateCamera(Vector3 position, Vector3 forward, Vector3 up);
}
