package com.studioussoftware.paperescaper.game;

import android.opengl.Matrix;

/**
 * Created by Robbie Wolfe on 8/9/2016.
 */
public class Vector3 {
    public float x, y, z;

    public Vector3() {}
    public Vector3(float x_, float y_, float z_) {
        set(x_, y_, z_);
    }

    public float[] asVec4Array() {
        return new float[] { x, y, z, 1 };
    }

    public void set(float x_, float y_, float z_) {
        x = x_;
        y = y_;
        z = z_;
    }

    public Vector3 added(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public static Vector3 added(Vector3 u, Vector3 v) {
        return u.added(v);
    }

    public Vector3 subbed(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public static Vector3 subbed(Vector3 u, Vector3 v) {
        return u.subbed(v);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns a new vector in the same direction as this with a length of 1
     * @return
     */
    public Vector3 normalized() {
        float length = length();
        return new Vector3(x / length, y / length, z / length);
    }

    public Vector3 scaled(float amount) {
        return new Vector3(x * amount, y * amount, z * amount);
    }

    public Vector3 reversed() {
        return scaled(-1f);
    }

    /**
     * Returns a 3D vector perpendicular to both this and other
     * @param other
     * @return
     */
    public Vector3 cross(Vector3 other) {
        return new Vector3(
            (y * other.z) - (z * other.y),
            (z * other.x) - (x * other.z),
            (x * other.y) - (y * other.x)
        );
    }

    public static Vector3 cross(Vector3 u, Vector3 v) {
        return u.cross(v);
    }

    /**
     * Returns a new Vector that is this rotated along the given vector the amount of the given angle
     * @param angle in degrees
     * @param rotationVec [x, y, z]
     */
    public Vector3 rotated(float angle, Vector3 rotationVec) {
        float[] rotMat = new float[16];
        float[] asArray = asVec4Array();
        float[] resultVec = new float[4];
        Matrix.setRotateM(rotMat, 0, angle, rotationVec.x, rotationVec.y, rotationVec.z);
        Matrix.multiplyMV(resultVec, 0, rotMat, 0, asArray, 0);
        return new Vector3(resultVec[0], resultVec[1], resultVec[2]);
    }

    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
