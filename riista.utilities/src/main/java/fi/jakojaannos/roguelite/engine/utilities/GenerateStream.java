package fi.jakojaannos.roguelite.engine.utilities;

import org.joml.Vector2i;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class GenerateStream {
    /**
     * Generates a stream containing all integer coordinates of the given region. The stream will have exactly <code>w *
     * h</code> elements.
     *
     * @param x from x-coordinate (inclusive)
     * @param y from y-coordinate (inclusive)
     * @param w to x-coordinate (exclusive)
     * @param h to y-coordinate (exclusive)
     *
     * @return Stream containing all coordinates of the region
     */
    public static Stream<Vector2i> ofCoordinates(
            final int x,
            final int y,
            final int w,
            final int h
    ) {
        return Stream.generate(new Supplier<Vector2i>() {
            private final Vector2i state = new Vector2i(x, y);
            private int ix = -1, iy = 0;

            @Override
            public Vector2i get() {
                ++this.ix;
                if (this.ix >= w) {
                    this.ix = 0;
                    ++this.iy;
                }

                return this.state.set(x + ix, y + iy);
            }
        }).limit(w * h);
    }
}
