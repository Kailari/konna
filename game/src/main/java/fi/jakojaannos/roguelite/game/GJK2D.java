package fi.jakojaannos.roguelite.game;

import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Shape;

public class GJK2D {
    private static final Logger LOG = LoggerFactory.getLogger(GJK2D.class);
    private static final int MAX_ITERATIONS = 100;
    private static final double TOLERANCE = 0.1;
    private static final double EPA_TOLERANCE = 0.00001;

    public static boolean checkCollision(
            final Transform transformA,
            final Shape shapeA,
            final Transform transformB,
            final Shape shapeB
    ) {
        final var direction = new Vector2d(1, 0);
        final var collisionSimplex = new ArrayList<Vector2d>(3);
        return checkCollision(transformA, shapeA, transformB, shapeB, direction, collisionSimplex);
    }

    public static Result getCollision(
            final Transform transformA,
            final Shape shapeA,
            final Transform transformB,
            final Shape shapeB
    ) {
        final var direction = new Vector2d(1, 0);
        final var collisionSimplex = new ArrayList<Vector2d>(3);
        final var isColliding = checkCollision(transformA,
                                               shapeA,
                                               transformB,
                                               shapeB,
                                               direction,
                                               collisionSimplex);

        if (isColliding) {
            if (collisionSimplex.size() != 3) {
                return Result.collision(0.0, new Vector2d(0.0));
            }

            // Determine simplex winding
            final var a = collisionSimplex.get(2);
            final var b = collisionSimplex.get(1);
            final var c = collisionSimplex.get(0);
            final var cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
            final var clockwise = cross <= 0.0;

            var iterations = MAX_ITERATIONS;
            while (iterations > 0) {
                final var edge = findClosestEdge(collisionSimplex, clockwise);

                final var p = minkowskiSupport(edge.normal, transformA, shapeA, transformB, shapeB, new Vector2d());

                final var depth = p.dot(edge.normal);
                if (depth - edge.distance < EPA_TOLERANCE) {
                    return Result.collision(depth, edge.normal);
                } else {
                    // Insert the `p` in between the points of the closest edge
                    collisionSimplex.add(edge.index, p);
                }
                --iterations;
            }

            LOG.warn("EPA breaking out due to iterating for too long!");
            return Result.collision(0.0, new Vector2d());
        }

        return Result.noCollision();
    }

    public static double getDistance(
            final Transform transformA,
            final Shape shapeA,
            final Transform transformB,
            final Shape shapeB,
            final Vector2d direction
    ) {
        final var simplex = new ArrayList<Vector2d>(2);
        simplex.add(minkowskiSupport(direction, transformA, shapeA, transformB, shapeB, new Vector2d()));
        simplex.add(minkowskiSupport(direction.negate(), transformA, shapeA, transformB, shapeB, new Vector2d()));

        // direction = closest a b
        closestPointToOrigin(simplex.get(1), simplex.get(0), direction);

        var iterations = MAX_ITERATIONS;
        while (iterations > 0) {
            assert simplex.size() == 2;

            // direction points towards the origin, flip it
            direction.negate();

            // If direction is a zero vector, shapes are touching
            if (direction.lengthSquared() == 0) {
                return 0.0;
            }

            final var c = minkowskiSupport(direction, transformA, shapeA, transformB, shapeB, new Vector2d());

            final var dc = c.dot(direction);
            final var da = simplex.get(1).dot(direction);

            // Stop if we are not making progress
            if (dc - da < TOLERANCE) {
                return direction.length();
            }

            final var p1 = closestPointToOrigin(simplex.get(1), c, new Vector2d());
            final var p2 = closestPointToOrigin(c, simplex.get(0), new Vector2d());

            if (p1.length() < p2.length()) {
                simplex.get(0).set(c);
                direction.set(p1);
            } else {
                simplex.get(1).set(c);
                direction.set(p2);
            }

            --iterations;
        }

        return Double.NaN;
    }

    private static boolean checkCollision(
            final Transform transformA,
            final Shape shapeA,
            final Transform transformB,
            final Shape shapeB,
            final Vector2d direction,
            final List<Vector2d> simplex
    ) {
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

    private static Edge findClosestEdge(final List<Vector2d> simplex, final boolean clockwise) {
        final var closest = new Edge();
        closest.distance = Double.MAX_VALUE;

        for (var i = 0; i < simplex.size(); ++i) {
            final var nextIndex = i + 1 == simplex.size() ? 0 : i + 1;
            final var a = simplex.get(i);
            final var b = simplex.get(nextIndex);

            final var edge = b.sub(a, new Vector2d());

            // Use vector per-product to find edge normal
            //noinspection SuspiciousNameCombination
            final Vector2d normal = clockwise
                    ? new Vector2d(edge.y, -edge.x)
                    : new Vector2d(-edge.y, edge.x);

            normal.normalize();
            final var distance = normal.dot(a);
            if (distance < closest.distance) {
                closest.distance = distance;
                closest.normal = normal;
                closest.index = nextIndex;
            }
        }

        return closest;
    }

    private static Vector2d minkowskiSupport(
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

    private static Vector2d closestPointToOrigin(final Vector2d a, final Vector2d b, final Vector2d result) {
        final var abx = b.x - a.x;
        final var aby = b.y - a.y;

        // We can only calculate the perpendicular depth if the projected point from pos to
        // AB is actually on the segment. Thus, need to check here that it is not past either
        // endpoint before calculating the depth.
        // "bp dot ab" >= 0
        if (abx * -b.x + aby * -b.y >= 0.0) {
            // pos is past the segment towards B, thus the closest point is B
            return b;
        }

        // "ap dot ab" <= 0
        if (abx * -a.x + aby * -a.y <= 0.0) {
            // pos is past the segment towards A, thus the closest point is A
            return a;
        }

        // Project the pos to the direction vector AB
        final var length = Math.sqrt(abx * abx + aby * aby);
        final var dx = abx / length;
        final var dy = aby / length;
        final var t = (-a.x * dx) + (-a.y * dy);
        return a.add(dx * t, dy * t, result);
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
            final var acPerpendicular = tripleProduct(ab, ac, ac);

            // The origin lies on the right side of A<->C
            if (acPerpendicular.dot(ao) >= 0.0) {
                simplex.remove(1);
                direction.set(acPerpendicular);
            } else {
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

    public static record Result(boolean collides, double depth, @Nullable Vector2d normal) {
        private static Result noCollision() {
            return new Result(false, Double.NaN, null);
        }

        private static Result collision(final double depth, final Vector2d normal) {
            return new Result(true, depth, normal);
        }
    }

    private static class Edge {
        public double distance;
        public Vector2d normal;
        public int index;
    }
}
