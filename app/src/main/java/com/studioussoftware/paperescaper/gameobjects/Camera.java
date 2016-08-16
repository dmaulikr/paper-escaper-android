package com.studioussoftware.paperescaper.gameobjects;

import android.opengl.Matrix;

import com.studioussoftware.paperescaper.game.Vector3;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class Camera {
    private Vector3 position = new Vector3();
    private Vector3 forward = new Vector3();
    private Vector3 up = new Vector3();

    public Camera() {}
    public Camera(Vector3 position_, Vector3 forward_, Vector3 up_) {
        position = position_;
        forward = forward_.normalized();
        up = up_.normalized();
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void setPosition(Vector3 pos) {
        position = pos;
    }

    public Vector3 getForward() {
        return forward;
    }

    public void roll(float angle) {

    }

    /**
     * Rotate the camera along the x axis in a clockwise direction
     * @param angle
     */
    public void pitch(float angle) {
        forward = forward.rotated(angle, new Vector3(-1, 0, 0)).normalized();

        // Fix the now incorrect up vector
        Vector3 right = Vector3.cross(forward, up);
        up = Vector3.cross(right, forward).normalized();
    }

    /**
     * Rotate the camera along the y axis in a clockwise direction
     * @param angle
     */
    public void yaw(float angle) {
        forward = forward.rotated(angle, new Vector3(0, -1, 0)).normalized();
    }

    public Vector3 getUp() {
        return up;
    }

    public void setUp(float x, float y, float z) {
        up = new Vector3(x, y, z).normalized();
    }

    public void setUp(Vector3 up_) {
        up = up_.normalized();
    }

    public Vector3 getRight() {
        return Vector3.cross(forward, up);
    }
}
