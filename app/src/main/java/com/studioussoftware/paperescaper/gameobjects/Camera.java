package com.studioussoftware.paperescaper.gameobjects;

import com.studioussoftware.paperescaper.model.Axis;
import com.studioussoftware.paperescaper.model.Vector3;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class Camera {
    private Vector3 position = new Vector3();
    private Vector3 forward = new Vector3();
    private Vector3 up = new Vector3();

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

    public void setPosition(Axis axis, float value) {
        switch (axis) {
            case X:
                position.x = value;
                break;
            case Y:
                position.y = value;
                break;
            case Z:
                position.z = value;
                break;
        }
    }

    public void setPosition(Vector3 pos) {
        position = pos;
    }

    /**
     * Using the values provided by the joystick, update the camera's X/Z position
     * @param angle forward = 0, right = -90, backward = -180, left = 90
     * @param magnitude [0 - 12.5]
     */
    public void walk(int angle, float magnitude) {
        // Move along the forward vector in the XZ plane
        Vector3 movementVec = forward.clone();
        movementVec.y = 0;

        // Rotated in the Y axis according to the angle and scaled according to the magnitude
        movementVec = movementVec.rotated(angle, Axis.Y).scaled(magnitude);

        position = position.added(movementVec);
    }

    public Vector3 getForward() {
        return forward;
    }

    public void roll(float angle) {
        if (angle == 0) return;
    }

    /**
     * Rotate the camera along the world x axis in a clockwise direction
     * @param angle
     */
    public void pitch(float angle) {
        if (angle == 0) return;

        // NOTE: If undesired Roll occurs when pitching/yawing, switch to using the local x axis, not the world x axis
        // forward = forward.rotated(angle, right).normalized();
        // See: http://gamedev.stackexchange.com/questions/103242/why-is-the-camera-tilting-around-the-z-axis-when-i-only-specified-x-and-y

        forward = forward.rotated(angle, Vector3.UNIT_X).normalized();

        // Fix the now incorrect up vector
        Vector3 right = Vector3.cross(forward, new Vector3(0, 1, 0)).normalized();
        up = Vector3.cross(right, forward).normalized();
    }

    /**
     * Rotate the camera along the world y axis in a clockwise direction
     * @param angle
     */
    public void yaw(float angle) {
        if (angle == 0) return;

        forward = forward.rotated(angle, Vector3.UNIT_Y).normalized();
    }

    public Vector3 getUp() {
        return up;
    }

    public void setUp(float x, float y, float z) {
        up = new Vector3(x, y, z).normalized();
    }

    public void setUp(Axis axis, float value) {
        switch (axis) {
            case X:
                up.x = value;
                break;
            case Y:
                up.y = value;
                break;
            case Z:
                up.z = value;
                break;
        }
    }

    public void setUp(Vector3 up_) {
        up = up_.normalized();
    }
}
