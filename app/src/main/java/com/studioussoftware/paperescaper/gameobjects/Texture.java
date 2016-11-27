package com.studioussoftware.paperescaper.gameobjects;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Created by Robbie Wolfe on 11/18/2016.
 * Given a Bitmap object, converts it to an OpenGL texture
 * Assumes responsibility to destroy the Bitmap
 */
public class Texture {
    private int textureID = -1;     // Stays this way if loading into OpenGL failed

    public Texture(Bitmap img) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        if (textures[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

            // Create nearest Filtered Texture
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            // could also do GL_CLAMP_TO_EDGE
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);

            textureID = textures[0];
        }

        // Recyle the Bitmap since it's data has been loaded into OpenGL
        img.recycle();
    }

    public int getTextureID() {
        return textureID;
    }
}
