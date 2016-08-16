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
    private final String vertexShaderCode =
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

    private final String fragmentShaderCode =
            "precision mediump float;" +    // how precise to be with floats
            "varying vec4 vColor;" +        // interpolated from the vertices
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private boolean glInitialized = false;
    private int glProgram;

    static final int COORDS_PER_VERTEX = 3;
    static float vertices[] = {
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
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3,
                                  4, 5, 6, 4, 6, 7};
    private final int vertexStride = COORDS_PER_VERTEX * Float.BYTES;

    private final int COORDS_PER_COLOR = 4;
    float colorFront[] = {1.0f, 0, 0, 1.0f}; // { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    float colorBack[] =  {0, 1.0f, 0, 1.0f}; //{ 0.76953125f, 0.63671875f, 0.22265625f, 1.0f };
    private final int colorStride = COORDS_PER_COLOR * Float.BYTES;

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer drawListBuffer;

    private final float[] ModelMatrix = new float[16];      // Transformations
    private static final float PAPER_RATIO = 11 / 8.5f;
    private static final float SCALE_Y = 1000;
    private static final float SCALE_X = SCALE_Y * PAPER_RATIO;
    private static final float SCALE_Z = 0.5f;

    private float pitch = 0.f;//90.f;
    private float yRotation = 0.f;

    // Move the sheet so the origin is in the middle of where the sheet falls
    private float xLocation = 0;
    private float yLocation = 0;//SCALE_Y;
    private float zLocation = 0;//-SCALE_Y;

    public PaperSheet() {
        initGeometry();
    }

    public void initGL() {
        if (!glInitialized) {
            // Set up Shaders
            int vertexShader = PaperGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = PaperGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            glProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(glProgram, vertexShader);
            GLES20.glAttachShader(glProgram, fragmentShader);
            GLES20.glLinkProgram(glProgram);
            glInitialized = true;
        }
    }

    private void initGeometry() {
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

    private void update() {
        //pitch += 0.5f;// = (pitch + 0.01f > 90) ? 90 : pitch + 0.01f;
        Matrix.setIdentityM(ModelMatrix, 0);
        Matrix.translateM(ModelMatrix, 0, xLocation, yLocation, zLocation);
        Matrix.rotateM(ModelMatrix, 0, pitch, 1, 0, 0);
        Matrix.rotateM(ModelMatrix, 0, yRotation, 0, 1, 0);
        Matrix.scaleM(ModelMatrix, 0, SCALE_X, SCALE_Y, SCALE_Z);
    }

    public void draw(float[] perspectiveMatrix, float[] vMatrix) {
        update();
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

    public void move(float x, float y) {
        pitch += y < 0 ? -1.0f : 1.0f;
        yRotation += x < 0 ? -1.0f : 1.0f;
    }

    public void zoom(float scaleFactor) {
        zLocation = (-20 * scaleFactor) - 2;
    }
}
