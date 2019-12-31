package fi.jakojaannos.roguelite.engine.lwjgl.view;

import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.UniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;

@Slf4j
public class LWJGLCamera extends Camera implements AutoCloseable {
    private static final double CAMERA_MOVE_EPSILON = 0.0001;

    private static final int MATRIX4F_SIZE_IN_BYTES = 16 * 4;
    private static final int VIEW_MATRIX_UBO_OFFSET = 0;
    private static final int PROJECTION_MATRIX_UBO_OFFSET = MATRIX4F_SIZE_IN_BYTES;

    @Getter private double targetScreenSizeInUnits = 32.0;
    private boolean targetSizeIsRespectiveToMinorAxis;

    private double viewportWidthInUnits;
    private double viewportHeightInUnits;

    @Override
    public double getVisibleAreaWidth() {
        return viewportWidthInUnits;
    }

    @Override
    public double getVisibleAreaHeight() {
        return viewportHeightInUnits;
    }

    @Getter private final Viewport viewport;

    private final Matrix4f projectionMatrix;
    private final Matrix4f screenProjectionMatrix;
    private boolean projectionMatrixDirty;

    private final Matrix4f viewMatrix;
    private boolean viewMatrixDirty;

    private final int worldCameraMatricesUbo;
    private final int screenCameraMatricesUbo;
    private final ByteBuffer cameraMatricesData;

    public void refreshMatricesIfDirty() {
        refreshViewMatrixIfDirty();
        refreshProjectionMatrixIfDirty();
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

    public void markProjectionMatrixDirty() {
        this.projectionMatrixDirty = true;
    }

    @Override
    public void useWorldCoordinates() {
        glBindBufferBase(GL_UNIFORM_BUFFER, UniformBufferObjectIndices.CAMERA, this.worldCameraMatricesUbo);
    }

    @Override
    public void useScreenCoordinates() {
        glBindBufferBase(GL_UNIFORM_BUFFER, UniformBufferObjectIndices.CAMERA, this.screenCameraMatricesUbo);
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

    public LWJGLCamera(final Viewport viewport) {
        super(new Vector2d(0.0, 0.0));
        this.viewport = viewport;

        this.projectionMatrix = new Matrix4f().identity();
        this.screenProjectionMatrix = new Matrix4f().identity();
        this.projectionMatrixDirty = true;

        this.viewMatrix = new Matrix4f();
        this.viewMatrixDirty = true;

        try (val stack = MemoryStack.stackPush()) {
            val ubos = stack.mallocInt(2);
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

    public void updateConfigurationFromState(final World world) {
        val camBounds = world.getOrCreateResource(CameraProperties.class);
        refreshTargetScreenSizeInUnits(camBounds.targetViewportSizeInWorldUnits, camBounds.targetViewportSizeRespectiveToMinorAxis);

        // FIXME: THIS BREAKS MVC ENCAPSULATION. Technically, we should queue task on the controller
        //  to make the change as we NEVER should mutate state on the view.
        camBounds.viewportWidthInWorldUnits = getVisibleAreaWidth();
        camBounds.viewportHeightInWorldUnits = getVisibleAreaHeight();
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
            val horizontalMajor = this.viewport.getWidthInPixels() > this.viewport.getHeightInPixels();
            double major = horizontalMajor ? this.viewport.getWidthInPixels() : this.viewport.getHeightInPixels();
            double minor = horizontalMajor ? this.viewport.getHeightInPixels() : this.viewport.getWidthInPixels();

            // TODO: Find such realTargetSize that pixelsPerUnit is a positive whole number to avoid
            //  aliasing.
            double realTargetSize = this.targetScreenSizeInUnits;
            val pixelsPerUnit = horizontalMajor
                    ? this.viewport.getWidthInPixels() / realTargetSize
                    : this.viewport.getHeightInPixels() / realTargetSize;

            val ratio = major / minor;
            this.viewportWidthInUnits = (float) Math.ceil(horizontalMajor
                                                                  ? ratio * realTargetSize
                                                                  : realTargetSize);
            this.viewportHeightInUnits = (float) Math.ceil(horizontalMajor
                                                                   ? realTargetSize
                                                                   : ratio * realTargetSize);
            this.projectionMatrix.setOrtho2D(
                    0.0f,
                    (float) viewportWidthInUnits,
                    (float) viewportHeightInUnits,
                    0.0f);
            this.screenProjectionMatrix.setOrtho2D(0.0f,
                                                   this.viewport.getWidthInPixels(),
                                                   this.viewport.getHeightInPixels(),
                                                   0.0f);

            LOG.trace("Refreshing projection matrix. New visible area size: {}×{}", this.viewportWidthInUnits, this.viewportHeightInUnits);
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
