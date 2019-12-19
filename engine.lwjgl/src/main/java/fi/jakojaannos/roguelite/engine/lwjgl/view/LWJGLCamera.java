package fi.jakojaannos.roguelite.engine.lwjgl.view;

import fi.jakojaannos.roguelite.engine.view.Camera;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

@Slf4j
public class LWJGLCamera extends Camera implements AutoCloseable {
    private static final double CAMERA_MOVE_EPSILON = 0.0001;

    @Getter(AccessLevel.PROTECTED)
    private double targetScreenSizeInUnits = 32.0;
    private boolean targetSizeIsRespectiveToMinorAxis;

    @Getter private double viewportWidthInUnits;
    @Getter private double viewportHeightInUnits;

    @Getter private int viewportWidthInPixels;
    @Getter private int viewportHeightInPixels;

    private final Matrix4f projectionMatrix;
    private boolean projectionMatrixDirty;

    private final Matrix4f viewMatrix;
    private boolean viewMatrixDirty;

    @Getter private final int cameraMatricesUbo;
    private final ByteBuffer cameraMatricesData;

    public Matrix4f getViewMatrix() {
        refreshViewMatrixIfDirty();
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        refreshProjectionMatrixIfDirty();
        return projectionMatrix;
    }

    protected void refreshTargetScreenSizeInUnits(
            double targetScreenSizeInUnits,
            boolean targetSizeIsRespectiveToMinorAxis
    ) {
        if (this.targetScreenSizeInUnits != targetScreenSizeInUnits || this.targetSizeIsRespectiveToMinorAxis != targetSizeIsRespectiveToMinorAxis) {
            this.projectionMatrixDirty = true;
            this.targetScreenSizeInUnits = targetScreenSizeInUnits;
            this.targetSizeIsRespectiveToMinorAxis = targetSizeIsRespectiveToMinorAxis;
        }
    }

    public void resizeViewport(int viewportWidth, int viewportHeight) {
        this.viewportWidthInPixels = viewportWidth;
        this.viewportHeightInPixels = viewportHeight;
        glViewport(0, 0, this.viewportWidthInPixels, this.viewportHeightInPixels);

        this.projectionMatrixDirty = true;
        LOG.info("Resizing viewport: {}x{}", this.viewportWidthInPixels, this.viewportHeightInPixels);
    }

    @Override
    public void setPosition(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        if (dx * dx + dy * dy > CAMERA_MOVE_EPSILON || this.viewMatrixDirty) {
            super.setPosition(x, y);
            this.viewMatrixDirty = true;
        }
    }

    public LWJGLCamera(int viewportWidth, int viewportHeight) {
        super(new Vector2d(0.0, 0.0));
        this.viewportWidthInPixels = viewportWidth;
        this.viewportHeightInPixels = viewportHeight;

        this.projectionMatrix = new Matrix4f().identity();
        this.projectionMatrixDirty = true;
        resizeViewport(viewportWidth, viewportHeight);

        this.viewMatrix = new Matrix4f();
        this.viewMatrixDirty = true;

        this.cameraMatricesUbo = glGenBuffers();
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * 16 * 4);

        this.viewMatrix.get(0, this.cameraMatricesData);
        this.projectionMatrix.get(16 * 4, this.cameraMatricesData);
        glBindBuffer(GL_UNIFORM_BUFFER, this.cameraMatricesUbo);
        glBufferData(GL_UNIFORM_BUFFER, this.cameraMatricesData, GL_DYNAMIC_DRAW);
        refreshViewMatrixIfDirty();
    }

    private void refreshUniforms() {
        this.viewMatrix.get(0, this.cameraMatricesData);
        this.projectionMatrix.get(16 * 4, this.cameraMatricesData);
        glBindBuffer(GL_UNIFORM_BUFFER, this.cameraMatricesUbo);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.cameraMatricesData);
    }

    private void refreshProjectionMatrixIfDirty() {
        if (this.projectionMatrixDirty) {
            LOG.trace("Refreshing projection matrix");

            val horizontalMajor = this.viewportWidthInPixels > this.viewportHeightInPixels;
            double major = horizontalMajor ? this.viewportWidthInPixels : this.viewportHeightInPixels;
            double minor = horizontalMajor ? this.viewportHeightInPixels : this.viewportWidthInPixels;

            // TODO: Find such realTargetSize that pixelsPerUnit is a positive whole number to avoid
            //  aliasing.
            double realTargetSize = this.targetScreenSizeInUnits;
            val pixelsPerUnit = horizontalMajor
                    ? this.viewportWidthInPixels / realTargetSize
                    : this.viewportHeightInPixels / realTargetSize;

            val ratio = major / minor;
            this.viewportWidthInUnits = (float) (horizontalMajor
                    ? ratio * realTargetSize
                    : realTargetSize);
            this.viewportHeightInUnits = (float) (horizontalMajor
                    ? realTargetSize
                    : ratio * realTargetSize);
            this.projectionMatrix.setOrtho2D(
                    0.0f,
                    (float) viewportWidthInUnits,
                    (float) viewportHeightInUnits,
                    0.0f);

            this.projectionMatrixDirty = false;
            refreshUniforms();
        }
    }

    private void refreshViewMatrixIfDirty() {
        if (this.viewMatrixDirty) {
            this.viewMatrix
                    .identity()
                    .translate((float) getX(), (float) getY(), 0.0f)
                    //.scale(this.zoom);
                    .invert();

            this.viewMatrixDirty = false;
            refreshUniforms();
        }
    }

    @Override
    public void close() {
        glDeleteBuffers(this.cameraMatricesUbo);
    }
}
