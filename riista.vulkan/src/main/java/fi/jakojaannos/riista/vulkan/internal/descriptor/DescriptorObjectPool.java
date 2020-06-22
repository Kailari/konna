package fi.jakojaannos.riista.vulkan.internal.descriptor;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.jakojaannos.riista.vulkan.util.RecreateCloseable;

public class DescriptorObjectPool<TDescriptor extends DescriptorObject, TKey> extends RecreateCloseable {
    private final DescriptorPool descriptorPool;

    private final Collection<TDescriptor> allDescriptors;

    private final Queue<TDescriptor> freeDescriptors;
    private final Map<TKey, TDescriptor> descriptors;
    private final BiConsumer<TKey, TDescriptor> descriptorUpdater;

    public DescriptorObjectPool(
            final DescriptorPool descriptorPool,
            final Function<DescriptorPool, TDescriptor> descriptorFactory,
            final BiConsumer<TKey, TDescriptor> descriptorUpdater,
            final int capacity
    ) {
        this.descriptorPool = descriptorPool;
        this.descriptorUpdater = descriptorUpdater;

        this.allDescriptors = Stream.generate(() -> descriptorFactory.apply(this.descriptorPool))
                                    .limit(capacity)
                                    .collect(Collectors.toUnmodifiableList());
        this.freeDescriptors = new ArrayDeque<>(this.allDescriptors);
        this.descriptors = new HashMap<>(this.freeDescriptors.size());
    }

    public void reset() {
        this.freeDescriptors.addAll(this.descriptors.values());
        this.descriptors.clear();
    }

    protected TDescriptor get(final TKey key) {
        return this.descriptors.computeIfAbsent(key, this::pollDescriptor);
    }

    private TDescriptor pollDescriptor(final TKey key) {
        // FIXME: Handle exhausted pool gracefully instead of crashing
        final var descriptor = this.freeDescriptors.remove();
        this.descriptorUpdater.accept(key, descriptor);

        return descriptor;
    }

    @Override
    protected void recreate() {
        reset();
        this.descriptorPool.tryRecreate();
        this.allDescriptors.forEach(RecreateCloseable::tryRecreate);
    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void close() {
        super.close();
        this.descriptors.clear();
        this.freeDescriptors.clear();
        this.descriptorPool.close();
        this.allDescriptors.forEach(RecreateCloseable::close);
    }
}
