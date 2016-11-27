package com.studioussoftware.paperescaper.gameobjects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.studioussoftware.paperescaper.game.PaperGLRenderer;
import com.studioussoftware.paperescaper.interfaces.IGameObject;
import com.studioussoftware.paperescaper.model.Constants;
import com.studioussoftware.paperescaper.model.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Robbie Wolfe on 8/8/2016.
 */
public class PaperSheet implements IGameObject {

    private static final long serialVersionUID = 928946056241175384L;

    ////////////////////////
    // OpenGL and Geometry variables common to all PaperSheets
    private static final String vertexShaderCode =
            "uniform vec2 uHoleLocation;" +
            "uniform float uHoleSize;" +
            "uniform mat4 uVMatrix;" +
            "uniform mat4 uMMatrix;" +
            "uniform mat4 uPMatrix;" +
            "attribute vec4 aVertexPosition;" +     // passed in
            "attribute vec4 aVertexColor;" +
            "attribute vec2 aTextureCoord;" +
            "varying vec4 vColor;" +
            "varying vec2 vTextureCoord;" +
            "varying vec2 vHoleLocation;" +   // transformed with model matrix
            "varying float vHoleSize;" +
            "void main() {" +
            "  gl_Position = uPMatrix * uVMatrix * uMMatrix * aVertexPosition;" +
            "  vColor = aVertexColor;" +    // pass the attributes to the fragment shader
            "  vTextureCoord = aTextureCoord;" +
            "  vHoleLocation = uHoleLocation;" +
            "  vHoleSize = uHoleSize;" +
            "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +    // how precise to be with floats
            "uniform sampler2D uTexture;" + // The input texture
            "varying vec4 vColor;" +        // interpolated from the vertices
            "varying vec2 vTextureCoord;" + // interpolated from the vertices
            "varying vec2 vHoleLocation;" +
            "varying float vHoleSize;" +
            "void main() {" +
            "  vec2 distanceFromHole = vTextureCoord - vHoleLocation;" +
            "  float distance = sqrt(distanceFromHole.x * distanceFromHole.x +" +
            "                        distanceFromHole.y * distanceFromHole.y);" +
            "  if (distance > vHoleSize) {" +   // outside of hole
            "    gl_FragColor = vColor * texture2D(uTexture, vTextureCoord);" +
            "  }" +
            "  else {" +
            "    gl_FragColor = vec4(0.0,0.0,0.0,0.0);" +   // see through
            "  }" +
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
                                               4, 5, 6, 4, 6, 7 };
    private static final int COORDS_PER_SIDE = 6;
    private static final int vertexStride = COORDS_PER_VERTEX * Constants.BYTES_PER_FLOAT;

    private static final int COORDS_PER_COLOR = 4;
    private static final float colorFront[] = {1, 1, 1, 1};
    private static final float colorBack[] =  {1, 1, 1, 1};
    private static final int colorStride = COORDS_PER_COLOR * Constants.BYTES_PER_FLOAT;

    private static final int COORDS_PER_TEXTURE = 2;
    private static final float textureCoords[] = {
            /**
             * Normally the origin is the bottom left and (1,1) is the top right (flipped Y axis from the primitive vertices)
             * In order for the pages to be sideways, have the origin be 'bottom right' of the sheet, and (1,1) being the 'top left'
             * Keep in mind that the 'Y axis' should be flipped since images have a different coordinate system than OpenGL
             */
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,

            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };
    private static final int textureCoordStride = COORDS_PER_TEXTURE * Constants.BYTES_PER_FLOAT;

    private static FloatBuffer vertexBuffer;
    private static FloatBuffer colorBuffer;
    private static FloatBuffer textureCoordBuffer;
    private static ShortBuffer drawListBuffer;

    private final float[] ModelMatrix = new float[16];      // Used for transformations
    private static final float PAPER_RATIO = 11 / 8.5f;     // Like A4 sheet of paper
    private static float SCALE_Y = 1000;                    // Half the size in the Y axis, as well as the other axes
    private static float SCALE_X = SCALE_Y * PAPER_RATIO;
    private static final float SCALE_Z = 0.5f;

    // Move the sheet so the origin is in the middle of where the sheet falls
    private static float xLocation = 0;
    private static float yLocation = SCALE_Y;       // Have the bottom of the sheet be at Y:0
    private static float zLocation = -SCALE_Y;

    public static final float MAX_HOLE_SIZE = 0.3f;
    public static final float MIN_HOLE_SIZE = 0.04f;
    private static final float MAX_HOLE_COORD = 1.0f;
    private static final float MIN_HOLE_COORD = 0.0f;

    public static final float MAX_ROTATION_SPEED = 2.0f;
    public static final float MIN_ROTATION_SPEED = 0.25f;
    private static float ROTATION_SPEED = MIN_ROTATION_SPEED;

    ////////////////////////
    // Variables unique to each PaperSheet
    private Vector3 holeTextureLocation = new Vector3();
    private Vector3 holeWorldLocation = new Vector3();
    private transient boolean holeLocationBufferInitialized = false;    // When deserialized will be false
    private transient FloatBuffer holeLocationBuffer;
    private final int holeLocationStride = COORDS_PER_TEXTURE * Constants.BYTES_PER_FLOAT;
    private float holeSize = 0;
    private float holeSizeWorld = 0;

    private float pitch = -90.f;
    private boolean rotating = false;
    private boolean shouldDelete = false;
    private boolean floor = false;

    private Texture frontTexture, backTexture;    // TODO: Serialization issues!

    /**
     * Sets up a PaperSheet with a hole
     * @param holeSize_ should be between MAX_HOLE_SIZE and MIN_HOLE_SIZE
     */
    public PaperSheet(float holeSize_, Texture front, Texture back) {
        holeSize = Math.max(Math.min(holeSize_, MAX_HOLE_SIZE), MIN_HOLE_SIZE);
        holeSizeWorld = holeSize * Math.max(SCALE_X, SCALE_Y);  // TODO: Since SCALE_Y is bigger, really it's an oval not a circle...?
        createHole();
        frontTexture = front;
        backTexture = back;
    }

    /**
     * Makes a Floor papersheet without a texture
     * TODO: Make it have a texture (and a hole)
     */
    public PaperSheet() {
        floor = true;
        pitch = 90.f;
    }

    private void createHole() {
        holeTextureLocation.x = (float) (Math.random() * MAX_HOLE_COORD) + MIN_HOLE_COORD;
        holeTextureLocation.y = (float) (Math.random() * MAX_HOLE_COORD) + MIN_HOLE_COORD;

        initializeHoleLocationBuffer();

        // Take the texture coordinates and convert them to world coordinates
        // This includes moving the origin from bottom left to the center of the sheet
        holeWorldLocation.x = (holeTextureLocation.x * SCALE_X * 2) - SCALE_X;
        holeWorldLocation.y = (holeTextureLocation.y * SCALE_Y * 2) - SCALE_Y;
    }

    private void initializeHoleLocationBuffer() {
        float[] holeLocationFloatArray = new float[] { holeTextureLocation.x, holeTextureLocation.y };
        ByteBuffer bb = ByteBuffer.allocateDirect(holeLocationStride);
        bb.order(ByteOrder.nativeOrder());
        holeLocationBuffer = bb.asFloatBuffer();
        holeLocationBuffer.put(holeLocationFloatArray);
        holeLocationBuffer.position(0);

        holeLocationBufferInitialized = true;
    }

    /**
     * Never use the holeLocationBuffer variable directly as it may not be initialized due to Serialization
     * @return
     */
    private FloatBuffer getHoleLocationBuffer() {
        if (!holeLocationBufferInitialized) {
            initializeHoleLocationBuffer();
        }
        return holeLocationBuffer;
    }

    /**
     * Only gives the value if CHEAT_MODE is on, otherwise it's no one's business
     * @return
     */
    public Vector3 getHoleWorldLocation() {
        if (Constants.CHEAT_MODE) {
            return holeWorldLocation.clone();
        }
        return null;
    }

    public static void initGL() {
        if (!glInitialized) {
            // Set up Shaders
            int vertexShader = PaperGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = PaperGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            glProgram = PaperGLRenderer.createGLProgram(new int[] { vertexShader, fragmentShader} );
            initGeometry();
            glInitialized = true;
        }
    }

    public static void resetGL() {
        glInitialized = false;
    }

    private static void initGeometry() {
        // Vertex Buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * Constants.BYTES_PER_FLOAT);
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

        // Color Buffer
        bb = ByteBuffer.allocateDirect(colors.length * Constants.BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // Texture Coordinate Buffer
        bb = ByteBuffer.allocateDirect(textureCoords.length * Constants.BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        textureCoordBuffer = bb.asFloatBuffer();
        textureCoordBuffer.put(textureCoords);
        textureCoordBuffer.position(0);

        // Draw Order Buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * Constants.BYTES_PER_SHORT);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        if (Constants.CHEAT_MODE) {
            //ROTATION_SPEED = MAX_ROTATION_SPEED;
        }
    }

    public Texture getBackTexture() {
        return backTexture;
    }

    public void setBackTexture(Texture texture) {
        backTexture = texture;
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

    public static void setFallingSpeed(float speed) {
        ROTATION_SPEED = speed;
    }

    /**
     * Returns true if the player is outside the hole and the top of the sheet is crushing them
     * @param playerX
     * @param playerY
     * @param playerZ
     * @return
     */
    public boolean isColliding(float playerX, float playerY, float playerZ) {
        // If page is falling forward and player is outside the hole
        if (!Constants.DEV_MODE && pitch > 0 && !isInHole(playerX, playerZ)) {
            final float PLAYER_HEIGHT_BUFFER = 10;
            // Make a vector along the sheet from the spine pointing up
            double radianPitch = Math.toRadians(pitch);
            Vector3 sheetVector = new Vector3(0, SCALE_Y * (float) Math.cos(radianPitch), SCALE_Y * (float) Math.sin(radianPitch));

            /**
			 * Use similar triangles to detect collision
             *        *
			 *	     /|		P = player
			 *	    X |		E = edge of page (page falling clockwise)
			 *     /| |		X = Point on the falling page above the player
			 *    / | |	    S = Spine of the book (The pages rotate around S)
			 *   /  | |     * = sheetVector (origin at S)
		     * S/___P_E
             *
             * This means sheetVector.z corresponds to S-->E, and playerZ should be S-->P
             * When X's y coordinate (P-->X) is smaller than the player's a collision occurs
             */

            playerZ += SCALE_Y;     // Make the player's position's origin be centered at spine of the book
            float triangleRatio = playerZ / sheetVector.z;      // Calculate how much smaller the player's triangle is
            float sheetHeightAtPlayer = triangleRatio * sheetVector.y;
            return sheetHeightAtPlayer < playerY + PLAYER_HEIGHT_BUFFER;
        }

        return false;
    }

    /**
     * Returns true if the player is inside the hole (if it was superimposed on the floor)
     * @param playerX
     * @param playerZ
     * @return
     */
    public boolean isInHole(float playerX, float playerZ) {
        // Use the formula for a circle with origin (h,k) with
        // radius r (x-h)^2 + (y-k)^2 = r^2 to see if inside
        // TODO: Use formula for an ellipse, since circle is stretched by PAPER_RATIO horizontally
        // ((x-h)/g)^2 + ((y-k)/j)^2 = r^2
        return Math.pow(playerX - holeWorldLocation.x, 2) +
                Math.pow(playerZ - holeWorldLocation.y, 2)
                <= Math.pow(holeSizeWorld, 2);
    }

    @Override
    public void update() {
        if (rotating) {
            pitch += ROTATION_SPEED;
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

        // Don't draw shapes facing away from the camera
        GLES20.glFrontFace(GLES20.GL_CCW); // Front face in counter-clockwise orientation
        GLES20.glEnable(GLES20.GL_CULL_FACE); // Enable cull face
        GLES20.glCullFace(GLES20.GL_BACK); // Cull the back face (don't display)

        // Enable transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int positionHandle = GLES20.glGetAttribLocation(glProgram, "aVertexPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        int colorHandle = GLES20.glGetAttribLocation(glProgram, "aVertexColor");
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, COORDS_PER_COLOR, GLES20.GL_FLOAT, false, colorStride, colorBuffer);

        int textureCoordHandle = GLES20.glGetAttribLocation(glProgram, "aTextureCoord");
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        GLES20.glVertexAttribPointer(textureCoordHandle, COORDS_PER_TEXTURE, GLES20.GL_FLOAT, false, textureCoordStride, textureCoordBuffer);

        // Pass the hole information to the shader
        int holeLocationHandle = GLES20.glGetUniformLocation(glProgram, "uHoleLocation");
        int holeSizeHandle = GLES20.glGetUniformLocation(glProgram, "uHoleSize");

        if (!floor) { // which doesn't have a hole
            GLES20.glUniform2fv(holeLocationHandle, 1, getHoleLocationBuffer());
        }
        GLES20.glUniform1f(holeSizeHandle, holeSize);

        // Get a handle to the vertex shader's matrices
        int perspectiveHandle = GLES20.glGetUniformLocation(glProgram, "uPMatrix");
        int modelHandle = GLES20.glGetUniformLocation(glProgram, "uMMatrix");
        int viewHandle = GLES20.glGetUniformLocation(glProgram, "uVMatrix");

        // Pass the projection matrix and model view transformation to the shader
        GLES20.glUniformMatrix4fv(perspectiveHandle, 1, false, perspectiveMatrix, 0);
        GLES20.glUniformMatrix4fv(modelHandle, 1, false, ModelMatrix, 0);
        GLES20.glUniformMatrix4fv(viewHandle, 1, false, vMatrix, 0);

        // Handle Textures
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // Draw the front of the sheet
        int textureID = frontTexture != null ? frontTexture.getTextureID() : 0;
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        drawListBuffer.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, COORDS_PER_SIDE, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Draw the back of the sheet
        textureID = backTexture != null ? backTexture.getTextureID() : 0;

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        drawListBuffer.position(COORDS_PER_SIDE);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, COORDS_PER_SIDE, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisable(GLES20.GL_TEXTURE_2D);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public boolean getShouldDelete() {
        return shouldDelete;
    }
}
