package com.studioussoftware.paperescaper.gameobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;
import com.studioussoftware.paperescaper.interfaces.IGameObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperSheet implements IGameObject {

    ////////////////////////
    // OpenGL and Geometry variables common to all PaperSheets
    private static final String vertexShaderCode =
            "uniform mat4 uVMatrix;" +
            "uniform mat4 uMMatrix;" +
            "uniform mat4 uPMatrix;" +
            "attribute vec4 aVertexPosition;" +     // passed in
            "attribute vec4 aVertexColor;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_Position = uPMatrix * uVMatrix * uMMatrix * aVertexPosition;" +
            "  vColor = aVertexColor;" +    // pass the vertex's color to the pixel shader
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +    // how precise to be with floats
            "varying vec4 vColor;" +        // interpolated from the vertices
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private static boolean glInitialized = false;
    private static int glProgram;

    private static final int COORDS_PER_VERTEX = 3;
    private static final float vertices[] = {
            // Front face (CC order)
            -1.0f,  1.0f, 1.0f,   // top left
            -1.0f, -1.0f, 1.0f,   // bottom left
             1.0f, -1.0f, 1.0f,   // bottom right
             1.0f,  1.0f, 1.0f,   // top right

            // Back face (CC order)
             1.0f,  1.0f, -1.0f,     // top right
             1.0f, -1.0f, -1.0f,     // bottom right
            -1.0f, -1.0f, -1.0f,     // bottom left
            -1.0f,  1.0f, -1.0f,     // top left
    };
    private static final short drawOrder[] = { 0, 1, 2, 0, 2, 3,
                                               4, 5, 6, 4, 6, 7};
    private static final int vertexStride = COORDS_PER_VERTEX * Float.BYTES;

    private static final int COORDS_PER_COLOR = 4;
    private static final float colorFront[] = {1.0f, 0, 0, 1.0f};
    private static final float colorBack[] =  {0, 1.0f, 0, 1.0f};
    private static final int colorStride = COORDS_PER_COLOR * Float.BYTES;

    private static FloatBuffer vertexBuffer;
    private static FloatBuffer colorBuffer;
    private static ShortBuffer drawListBuffer;

    private final float[] ModelMatrix = new float[16];      // Used for transformations
    private static final float PAPER_RATIO = 11 / 8.5f;     // Like A4 sheet of paper
    private static float SCALE_Y = 1000;                    // Half the size in the Y axis, as well as the other axes
    private static float SCALE_X = SCALE_Y * PAPER_RATIO;
    private static final float SCALE_Z = 0.5f;

    // Move the sheet so the origin is in the middle of where the sheet falls
    private static float xLocation = 0;
    private static float yLocation = SCALE_Y;
    private static float zLocation = -SCALE_Y;

    ////////////////////////
    // Variables unique to each PaperSheet
    private float pitch = -90.f;
    private boolean rotating = false;
    private boolean shouldDelete = false;
    private boolean floor = false;

    public PaperSheet() {}

    public PaperSheet(boolean floor_) {
        floor = floor_;
        if (floor) {
            pitch = 90.f;
        }
    }

    public static void initGL() {
        if (!glInitialized) {
            // Set up Shaders
            int vertexShader = PaperGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = PaperGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            glProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram, vertexShader);
            GLES20.glAttachShader(glProgram, fragmentShader);
            GLES20.glLinkProgram(glProgram);
            initGeometry();
            glInitialized = true;
        }
    }

    private static void initGeometry() {
        // Vertex Buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * Float.BYTES);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // Initialize Colors
        float[] colors = new float[(int)((vertices.length / (float) COORDS_PER_VERTEX) * COORDS_PER_COLOR)];
        int k = 0;
        for (int i = 0; i < vertices.length; i += COORDS_PER_VERTEX) {
            float[] colorArray = (i < vertices.length / 2.0) ? colorFront : colorBack;
            for (int j = 0; j < colorArray.length; ++j) {
                colors[k + j] = colorArray[j];
            }
            k += COORDS_PER_COLOR;
        }
        colors[0] = 0; colors[1] = 0; colors[2] = 1.0f;

        // Color Buffer
        bb = ByteBuffer.allocateDirect(colors.length * Float.BYTES);
        bb.order(ByteOrder.nativeOrder());
        colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // Draw Order Buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * Short.BYTES);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
    }

    /**
     * Sets the PaperSize with the given height, width is automatically calculated
     * @param height
     */
    public static void setPaperSize(float height) {
        SCALE_Y = height;
        SCALE_X = SCALE_Y * PAPER_RATIO;
        yLocation = SCALE_Y;
        zLocation = -SCALE_Y;
    }

    public static float getPaperWidth() {
        return SCALE_X;
    }

    public void startRotating() {
        rotating = true;
    }

    @Override
    public void update() {
        if (rotating) {
            pitch += 0.25f;
            if (pitch >= 90.f) {
                rotating = false;
                shouldDelete = true;
            }
        }
    }

    private void setupModelMatrix() {
        Matrix.setIdentityM(ModelMatrix, 0);

        // Transformations should be performed backwards

        // Real translation
        Matrix.translateM(ModelMatrix, 0, xLocation, yLocation, zLocation);

        // Translate back to origin
        Matrix.translateM(ModelMatrix, 0, 0, -SCALE_Y, 0);

        // Rotate
        Matrix.rotateM(ModelMatrix, 0, pitch, 1, 0, 0);

        // Translate in order to rotate at bottom of the sheet
        Matrix.translateM(ModelMatrix, 0, 0, SCALE_Y, 0);

        // Make it the right size
        Matrix.scaleM(ModelMatrix, 0, SCALE_X, SCALE_Y, SCALE_Z);
    }

    @Override
    public void draw(float[] perspectiveMatrix, float[] vMatrix) {
        setupModelMatrix();

        GLES20.glUseProgram(glProgram);

        GLES20.glFrontFace(GLES20.GL_CCW); // Front face in counter-clockwise orientation
        GLES20.glEnable(GLES20.GL_CULL_FACE); // Enable cull face
        GLES20.glCullFace(GLES20.GL_BACK); // Cull the back face (don't display)

        int positionHandle = GLES20.glGetAttribLocation(glProgram, "aVertexPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int colorHandle = GLES20.glGetAttribLocation(glProgram, "aVertexColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COORDS_PER_COLOR, GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        // Get a handle to the vertex shader's matrices
        int perspectiveHandle = GLES20.glGetUniformLocation(glProgram, "uPMatrix");
        int modelHandle = GLES20.glGetUniformLocation(glProgram, "uMMatrix");
        int viewHandle = GLES20.glGetUniformLocation(glProgram, "uVMatrix");

        // Pass the projection matrix and model view transformation to the shader
        GLES20.glUniformMatrix4fv(perspectiveHandle, 1, false, perspectiveMatrix, 0);
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, ModelMatrix, 0);
        GLES20.glUniformMatrix4fv(viewHandle, 1, false, vMatrix, 0);

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public boolean getShouldDelete() {
        return shouldDelete;
    }
}
