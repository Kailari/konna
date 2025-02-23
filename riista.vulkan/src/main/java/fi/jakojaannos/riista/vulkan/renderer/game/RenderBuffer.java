package fi.jakojaannos.riista.vulkan.renderer.game;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import fi.jakojaannos.riista.view.Presentable;

public class RenderBuffer<TOutput extends Presentable> implements Iterable<TOutput> {
    private final Supplier<TOutput> elementFactory;

    private TOutput[] elements;
    private int position;

    public RenderBuffer(
            final Supplier<TOutput> elementFactory,
            final IntFunction<TOutput[]> elementStorageFactory
    ) {
        this.elementFactory = elementFactory;

        this.elements = elementStorageFactory.apply(0);
        grow(16);
    }

    private void grow(final int increment) {
        final var firstNewIndex = this.elements.length;

        final var size = this.elements.length + increment;
        this.elements = Arrays.copyOf(this.elements, size);

        for (int i = firstNewIndex; i < this.elements.length; i++) {
            this.elements[i] = this.elementFactory.get();
        }
    }

    public void reset() {
        this.position = 0;
    }

    public synchronized TOutput get() {
        if (this.position == this.elements.length) {
            // FIXME: Benchmark to determine a good growth policy
            grow(1);
        }

        final var result = this.elements[this.position];
        ++this.position;

        result.reset();
        return result;
    }

    @Override
    public Iterator<TOutput> iterator() {
        return new Iterator<>() {
            private int position;

            @Override
            public boolean hasNext() {
                return this.position < RenderBuffer.this.position;
            }

            @Override
            public TOutput next() {
                final var next = RenderBuffer.this.elements[this.position];
                ++this.position;

                return next;
            }
        };
    }

    @Override
    public Spliterator<TOutput> spliterator() {
        return Spliterators.spliterator(this.elements, 0, this.position, 0);
    }
}
