package com.studioussoftware.paperescaper.model;

/**
 * Created by Robbie Wolfe on 9/15/2016.
 */
public class BoundsChecker {
    private float boundWidth, boundHeight;

    /**
     * The origin is in the middle of the sheet, using a buffer so the player doesn't
     * walk right to the edge, the bounds are [-width + buffer, width - buffer] for
     * the x axis and [-height + buffer, height - buffer] for the z axis
     * @param width
     * @param height
     * @param buffer
     */
    public BoundsChecker(float width, float height, float buffer) {
        boundWidth = width - buffer;
        boundHeight = height - buffer;
    }

    /**
     * Determines if moving the given position by the given vector will be inside the
     * game bounds or outside
     * @param position
     * @param movementVec
     * @return
     */
    public boolean withinBounds(Vector3 position, Vector3 movementVec) {
        float newX = position.x + movementVec.x;
        float newZ = position.z + movementVec.z;

        return (newX > -boundWidth && newX < boundWidth) && (newZ > -boundHeight && newZ < boundHeight);
    }
}
