package fi.jakojaannos.roguelite.game;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Shape;

public class GJK2D {
    private static final Transform DEFAULT_TRANSFORM = new Transform();
    private static final Vector2d tmpSupportA = new Vector2d();
    private static final Vector2d tmpSupportB = new Vector2d();
    private static final Vector2d tmpDirection = new Vector2d();

    private static final int MAX_ITERATIONS = 100;

    public static boolean intersects(
            final Transform transformA,
            final Shape shapeA,
            final Shape shapeB,
            final Vector2d initialDirection
    ) {
        return intersects(transformA, shapeA, DEFAULT_TRANSFORM, shapeB, initialDirection);
    }

    public static Vector2d minkowskiSupport(
            final Vector2d direction,
            final Transform transformA,
            final Shape shapeA,
            final Shape shapeB,
            final Vector2d result
    ) {
        return minkowskiSupport(direction, transformA, shapeA, DEFAULT_TRANSFORM, shapeB, result);
    }

    public static Vector2d minkowskiSupport(
            final Vector2d direction,
            final Transform transformA,
            final Shape shapeA,
            final Transform transformB,
            final Shape shapeB,
            final Vector2d result
    ) {
        // minkowskiSupport = a.support(direction) - b.support(-1 × direction)
        final var negatedDirection = direction.negate(new Vector2d());
        final var supportA = shapeA.supportPoint(transformA, direction, new Vector2d());
        final var supportB = shapeB.supportPoint(transformB, negatedDirection, new Vector2d());

        return supportA.sub(supportB, result);
    }

    public static boolean intersects(
            final Transform transformA,
            final Shape shapeA,
            final Transform transformB,
            final Shape shapeB,
            final Vector2d initialDirection
    ) {
        List<Vector2d> simplex = new ArrayList<>(3);

        final var direction = tmpDirection.set(initialDirection);
        if (direction.lengthSquared() == 0.0) {
            direction.set(1.0, 0.0);
        }

        // Select first support point
        simplex.add(minkowskiSupport(direction, transformA, shapeA, transformB, shapeB, new Vector2d()));

        // Fail fast if the support point is not past the origin
        if (simplex.get(0).dot(direction) <= 0.0) {
            return false;
        }

        // Negate the direction to get a point on the opposite side
        direction.negate();
        var iterations = MAX_ITERATIONS;
        while (iterations-- > 0) {
            final var support = minkowskiSupport(direction, transformA, shapeA, transformB, shapeB, new Vector2d());
            simplex.add(support);

            // Due to the way points are selected, if the selected point did not move past origin
            // in the current direction, we know for sure that the Minkowski Sum does not contain
            // the origin. We can cheaply check moving past some point by taking the dot product.
            if (support.dot(direction) <= 0) {
                return false;
            }
            // If we did not conclude that the origin is outside of the Minkowski Sum, iterate
            // our simplex. If next iteration step is able to contain the origin inside the simplex,
            // return true, otherwise continue iterating.
            else {
                if (checkIfSimplexContainsTheOriginAndUpdateDirection(simplex, direction)) {
                    return true;
                }
            }
        }

        return true;
    }

    private static boolean checkIfSimplexContainsTheOriginAndUpdateDirection(
            final List<Vector2d> simplex,
            final Vector2d direction
    ) {
        final var a = simplex.get(simplex.size() - 1);
        final var ao = a.negate(new Vector2d());

        // 3 points, triangle
        if (simplex.size() == 3) {
            final var b = simplex.get(1);
            final var c = simplex.get(0);

            // Edges
            final var ab = b.sub(a, new Vector2d());
            final var ac = c.sub(a, new Vector2d());

            // Edge normals
//            final var acPerpendicular = new Vector2d();
//            final var abacDot = ab.x * ac.y - ac.x * ab.y;
//            acPerpendicular.set(-ac.y * abacDot,
//                                ac.x * abacDot);
            final var acPerpendicular = tripleProduct(ab, ac, ac);

            // The origin lies on the right side of A<->C
            if (acPerpendicular.dot(ao) >= 0.0) {
                simplex.remove(1);
                direction.set(acPerpendicular);
            } else {
//                final var abPerpendicular = new Vector2d();
//                abPerpendicular.set(ab.y * abacDot,
//                                    -ab.x * abacDot);
                final var abPerpendicular = tripleProduct(ac, ab, ab);

                // The origin is in the central region.
                if (abPerpendicular.dot(ao) < 0.0) {
                    return true;
                }
                // The origin lies between A and B
                else {
                    simplex.remove(0);
                    direction.set(abPerpendicular);
                }
            }
        }
        // 2 points, line
        else {
            final var b = simplex.get(0);
            final var ab = b.sub(a, new Vector2d());

            final var abPerpendicular = tripleProduct(ab, ao, ab);
            direction.set(abPerpendicular);

            if (direction.lengthSquared() <= 0.00001) {
                direction.set(ab.perpendicular());
            }
        }

        return false;
    }

    private static Vector2d tripleProduct(final Vector2d a, final Vector2d b, final Vector2d c) {
        final var ac = a.x * c.x + a.y * c.y;
        final var bc = b.x * c.x + b.y * c.y;
        return new Vector2d(b.x * ac - a.x * bc,
                            b.y * ac - a.y * bc);
    }
}
