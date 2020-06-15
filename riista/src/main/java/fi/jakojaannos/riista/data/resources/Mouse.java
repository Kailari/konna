package fi.jakojaannos.riista.data.resources;

import org.joml.*;

public class Mouse {
    public final Vector2d position = new Vector2d(-999.0, -999.0);
    public boolean clicked;

    /**
     * Finds the point under the cursor, projected on to the ground plane. Ground plane is xy-plane with z coordinate
     * zero.
     * <p>
     * Performs a ray-cast from camera position towards direction specified by the mouse cursor's relative position to
     * the center of the screen.
     *
     * @param cameraProperties camera/viewport properties
     *
     * @return x/y-coordinates of the point in world, under the cursor
     */
    public Vector2d calculatePositionUnderCursor(final CameraProperties cameraProperties) {
        // this.position is already in NDC, just pack it into vec4 and transform to view-world-space
        // NOTE: z was -1 in the tutorial. If something breaks, try changing that.
        final var homogenousClipCoordinates = new Vector4f((float) this.position.x,
                                                           (float) this.position.y,
                                                           0.0f,
                                                           1.0f);
        final var viewCoordinates = transformToView(homogenousClipCoordinates, cameraProperties);
        final var worldCoordinates = transformToWorld(viewCoordinates, cameraProperties);

        final var eyePosition = cameraProperties.getPosition();

        final var direction = new Vector3f(worldCoordinates.x,
                                           worldCoordinates.y,
                                           worldCoordinates.z);
        final var t = Intersectionf.intersectRayPlane(eyePosition,
                                                      direction,
                                                      new Vector3f(0.0f),
                                                      new Vector3f(0.0f, 0.0f, 1.0f),
                                                      0.0001f);
        final var resultVec3 = new Vector3f(eyePosition).add(direction.mul(t));
        return new Vector2d(resultVec3.x, resultVec3.y);
    }

    private static Vector4f transformToView(
            final Vector4f clipCoordinates,
            final CameraProperties cameraProperties
    ) {
        final var inverseProjection = cameraProperties.inverseProjection;
        final var viewCoords = clipCoordinates.mul(inverseProjection);
        return new Vector4f(viewCoords.x, viewCoords.y, -1.0f, 0.0f);
    }

    private static Vector4f transformToWorld(
            final Vector4f position,
            final CameraProperties cameraProperties
    ) {
        final var worldCoordinates = position.mul(cameraProperties.getInverseViewMatrix());
        if (worldCoordinates.lengthSquared() > 0.0) {
            worldCoordinates.normalize();
        }

        return worldCoordinates;
    }
}
