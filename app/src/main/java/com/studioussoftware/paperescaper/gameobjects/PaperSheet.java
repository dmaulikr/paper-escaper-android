package com.studioussoftware.paperescaper.gameobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperSheet {
    private final String vertexShaderCode =
            "uniform mat4 uVMatrix;" +
            "uniform mat4 uMMatrix;" +
            "uniform mat4 uPMatrix;" +
            "attribute vec4 vPosition;" +     // passed in
            "void main() {" +
            "  gl_Position = uPMatrix * uVMatrix * uMMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +    // how precise to be with floats
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final int glProgram;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f, 0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f, 0.5f, 0.0f    // top right
    };

    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexCount = drawOrder.length; //squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;     // 4 bytes per vertex

    private final float[] ModelMatrix = new float[16];      // Transformations

    public PaperSheet() {
        // Set up buffers
        // 4 bytes per float, use hardware's native byte order
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        // Create a floating point buffer from the Byte buffer
        vertexBuffer = bb.asFloatBuffer();
        // Add the coordinates
        vertexBuffer.put(squareCoords);
        // Read first position
        vertexBuffer.position(0);

        // 2 bytes per short
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());

        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Set up Shaders
        int vertexShader = PaperGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = PaperGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        glProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);
        GLES20.glLinkProgram(glProgram);

        Matrix.setIdentityM(ModelMatrix, 0);
    }

    public void draw(float[] projectionMatrix, float[] vMatrix) {
        // Add program to the OpenGL environment
        GLES20.glUseProgram(glProgram);

        // Get handle to vertex shader's vPosition member
        int positionHandle = GLES20.glGetAttribLocation(glProgram, "vPosition");

        // Enable a handle to the square's vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the square coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        // Get a handle to the fragment shader's vColor member
        int colorHandle = GLES20.glGetUniformLocation(glProgram, "vColor");

        // Set color for drawing the square
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Handle transformations
        Matrix.rotateM(ModelMatrix, 0, -1.0f, 0, 0, 1);

        // Get a handle to the vertex shader's matrices
        int projectionHandle = GLES20.glGetUniformLocation(glProgram, "uPMatrix");
        int modelHandle = GLES20.glGetUniformLocation(glProgram, "uMMatrix");
        int viewHandle = GLES20.glGetUniformLocation(glProgram, "uVMatrix");

        // Pass the projection matrix and model view transformation to the shader
        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projectionMatrix, 0);
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, ModelMatrix, 0);
        GLES20.glUniformMatrix4fv(viewHandle, 1, false, vMatrix, 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
