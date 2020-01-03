package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

public interface Shape {
    Vector2d[] getVerticesInLocalSpace(
            Transform transform
    );

    default Rectangled getBounds(final Transform transform) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        val vertices = getVerticesInLocalSpace(transform);
        for (val vertex : vertices) {
            minX = Math.min(minX, transform.position.x + vertex.x);
            maxX = Math.max(maxX, transform.position.x + vertex.x);
            minY = Math.min(minY, transform.position.y + vertex.y);
            maxY = Math.max(maxY, transform.position.y + vertex.y);
        }

        return new Rectangled(minX, minY, maxX, maxY);
    }

    default Vector2d supportPoint(
            final Transform transform,
            final Vector2d direction,
            final Vector2d result
    ) {
        val normDirection = direction.normalize(new Vector2d());
        val vertices = getVerticesInLocalSpace(transform);

        var maxProduct = normDirection.dot(vertices[0]);
        var index = 0;
        for (var i = 1; i < vertices.length; ++i) {
            val product = normDirection.dot(vertices[i]);
            if (product > maxProduct) {
                maxProduct = product;
                index = i;
            }
        }

        return result.set(vertices[index]).add(transform.position);
    }
}
