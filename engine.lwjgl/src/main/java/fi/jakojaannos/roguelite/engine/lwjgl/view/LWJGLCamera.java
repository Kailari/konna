package fi.jakojaannos.roguelite.engine.lwjgl.view;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

public class LWJGLCamera extends Camera {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLCamera.class);

    private static final double CAMERA_MOVE_EPSILON = 0.0001;

    private static final int MATRIX4F_SIZE_IN_BYTES = 16 * 4;
    private static final int VIEW_MATRIX_UBO_OFFSET = 0;
    private static final int PROJECTION_MATRIX_UBO_OFFSET = MATRIX4F_SIZE_IN_BYTES;
    private final Matrix4f projectionMatrix;
    private final Matrix4f screenProjectionMatrix;
    private final Matrix4f viewMatrix;
    private final int worldCameraMatricesUbo;
    private final int screenCameraMatricesUbo;
    private final ByteBuffer cameraMatricesData;
    private double targetScreenSizeInUnits = 32.0;
    private boolean targetSizeIsRespectiveToMinorAxis;
    private double viewportWidthInUnits;
    private double viewportHeightInUnits;
    private boolean projectionMatrixDirty;
    private boolean viewMatrixDirty;

    @Override
    public double getVisibleAreaWidth() {
        return this.viewportWidthInUnits;
    }

    @Override
    public double getVisibleAreaHeight() {
        return this.viewportHeightInUnits;
    }

    public LWJGLCamera(final Viewport viewport) {
        super(new Vector2d(0.0, 0.0), viewport);

        this.projectionMatrix = new Matrix4f().identity();
        this.screenProjectionMatrix = new Matrix4f().identity();
        this.projectionMatrixDirty = true;

        this.viewMatrix = new Matrix4f();
        this.viewMatrixDirty = true;

        try (final var stack = MemoryStack.stackPush()) {
            final var ubos = stack.mallocInt(2);
            glGenBuffers(ubos);
            this.worldCameraMatricesUbo = ubos.get(0);
            this.screenCameraMatricesUbo = ubos.get(1);
        }
        this.cameraMatricesData = MemoryUtil.memAlloc(2 * MATRIX4F_SIZE_IN_BYTES);

        for (int i = 0; i < this.cameraMatricesData.capacity(); ++i) {
            this.cameraMatricesData.put(i, (byte) 0);
        }
        glBindBuffer(GL_UNIFORM_BUFFER, this.worldCameraMatricesUbo);
        glBufferData(GL_UNIFORM_BUFFER, this.cameraMatricesData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_UNIFORM_BUFFER, this.screenCameraMatricesUbo);
        glBufferData(GL_UNIFORM_BUFFER, this.cameraMatricesData, GL_DYNAMIC_DRAW);

        refreshUniforms();

        refreshViewMatrixIfDirty();
        useWorldCoordinates();
    }

    public void refreshMatricesIfDirty() {
        refreshViewMatrixIfDirty();
        refreshProjectionMatrixIfDirty();
    }

    protected void refreshTargetScreenSizeInUnits(
            final double targetScreenSizeInUnits,
            final boolean targetSizeIsRespectiveToMinorAxis
    ) {
        final var screenSizeIsDirty = this.targetScreenSizeInUnits != targetScreenSizeInUnits;
        final var minorAxisSizeIsDirty = this.targetSizeIsRespectiveToMinorAxis != targetSizeIsRespectiveToMinorAxis;
        if (screenSizeIsDirty || minorAxisSizeIsDirty) {
            this.projectionMatrixDirty = true;
            this.targetScreenSizeInUnits = targetScreenSizeInUnits;
            this.targetSizeIsRespectiveToMinorAxis = targetSizeIsRespectiveToMinorAxis;
        }
    }

    @Override
    public void resize(final int width, final int height) {
        this.projectionMatrixDirty = true;
    }

    @Override
    public void useWorldCoordinates() {
        glBindBufferBase(GL_UNIFORM_BUFFER, EngineUniformBufferObjectIndices.CAMERA, this.worldCameraMatricesUbo);
    }

    @Override
    public void useScreenCoordinates() {
        glBindBufferBase(GL_UNIFORM_BUFFER, EngineUniformBufferObjectIndices.CAMERA, this.screenCameraMatricesUbo);
    }

    @Override
    public void setPosition(final double x, final double y) {
        final double dx = x - getX();
        final double dy = y - getY();
        if (dx * dx + dy * dy > CAMERA_MOVE_EPSILON || this.viewMatrixDirty) {
            super.setPosition(x, y);
            this.viewMatrixDirty = true;
        }

        refreshMatricesIfDirty();
    }

    @Override
    public void updateConfigurationFromState(final GameState state) {
        super.updateConfigurationFromState(state);

        final var world = state.world();
        final var cameraProperties = world.fetchResource(CameraProperties.class);
        refreshTargetScreenSizeInUnits(cameraProperties.targetViewportSizeInWorldUnits,
                                       cameraProperties.targetViewportSizeRespectiveToMinorAxis);
        refreshMatricesIfDirty();
    }

    private void refreshUniforms() {
        this.viewMatrix.get(VIEW_MATRIX_UBO_OFFSET, this.cameraMatricesData);
        this.projectionMatrix.get(PROJECTION_MATRIX_UBO_OFFSET, this.cameraMatricesData);
        glBindBuffer(GL_UNIFORM_BUFFER, this.worldCameraMatricesUbo);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.cameraMatricesData);

        new Matrix4f().identity().get(0, this.cameraMatricesData);
        this.screenProjectionMatrix.get(16 * 4, this.cameraMatricesData);
        glBindBuffer(GL_UNIFORM_BUFFER, this.screenCameraMatricesUbo);
        glBufferSubData(GL_UNIFORM_BUFFER, 0, this.cameraMatricesData);
    }

    private void refreshProjectionMatrixIfDirty() {
        if (this.projectionMatrixDirty) {
            final var horizontalMajor = this.getViewport().getWidthInPixels() > this.getViewport().getHeightInPixels();
            final double major = horizontalMajor
                    ? this.getViewport().getWidthInPixels()
                    : this.getViewport().getHeightInPixels();
            final double minor = horizontalMajor
                    ? this.getViewport().getHeightInPixels()
                    : this.getViewport().getWidthInPixels();

            // TODO: Find such realTargetSize that pixelsPerUnit is a positive whole number to avoid
            //  aliasing.
            final double realTargetSize = this.targetScreenSizeInUnits;
            final var pixelsPerUnit = horizontalMajor
                    ? this.getViewport().getWidthInPixels() / realTargetSize
                    : this.getViewport().getHeightInPixels() / realTargetSize;

            final var ratio = major / minor;
            this.viewportWidthInUnits = (float) Math.ceil(horizontalMajor
                                                                  ? ratio * realTargetSize
                                                                  : realTargetSize);
            this.viewportHeightInUnits = (float) Math.ceil(horizontalMajor
                                                                   ? realTargetSize
                                                                   : ratio * realTargetSize);
            final var halfWidth = (float) (this.viewportWidthInUnits / 2.0);
            final var halfHeight = (float) (this.viewportHeightInUnits / 2.0);
            this.projectionMatrix.setOrtho2D(
                    -halfWidth,
                    halfWidth,
                    halfHeight,
                    -halfHeight);
            this.screenProjectionMatrix.setOrtho2D(0.0f,
                                                   this.getViewport().getWidthInPixels(),
                                                   this.getViewport().getHeightInPixels(),
                                                   0.0f);

            LOG.trace("Refreshing projection matrix. New visible area size: {}×{}",
                      this.viewportWidthInUnits,
                      this.viewportHeightInUnits);
            this.projectionMatrixDirty = false;
            refreshUniforms();
        }
    }

    private void refreshViewMatrixIfDirty() {
        if (this.viewMatrixDirty) {
            this.viewMatrix.identity()
                           .translate((float) getX(), (float) getY(), 0.0f)
                           //.scale(this.zoom);
                           .invert();

            this.viewMatrixDirty = false;
            refreshUniforms();
        }
    }

    @Override
    public void close() {
        glDeleteBuffers(this.screenCameraMatricesUbo);
        glDeleteBuffers(this.worldCameraMatricesUbo);
        MemoryUtil.memFree(this.cameraMatricesData);
    }
}
