package fi.jakojaannos.roguelite.engine.view.dispatcher;

import java.lang.reflect.ParameterizedType;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.ParsedRequirements;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.SystemInputRecord;
import fi.jakojaannos.roguelite.engine.view.EcsRenderAdapter;
import fi.jakojaannos.roguelite.engine.view.RenderDispatcher;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;

import static org.lwjgl.system.MemoryUtil.memAlloc;

@SuppressWarnings("rawtypes")
public class RenderDispatcherImpl implements RenderDispatcher {
    private static final int MAX_PER_BATCH = 256;

    private final Map<Class<? extends EcsRenderAdapter>, ByteBuffer> adapterBuffers = new HashMap<>();
    private final Map<Class, ParsedRequirements> adapterRequirements;

    private final Collection<EcsRenderAdapter<?, ?>> adapters;

    public RenderDispatcherImpl(final Collection<EcsRenderAdapter<?, ?>> adapters) {
        this.adapters = List.copyOf(adapters);
        this.adapterRequirements = this.adapters
                .stream()
                .collect(Collectors.toUnmodifiableMap(EcsRenderAdapter::getClass,
                                                      RenderDispatcherImpl::resolveRequirements));
    }

    @Override
    public void render(final GameState state, final long accumulator) {
        for (final var adapter : this.adapters) {
            render(adapter, state, accumulator);
        }
    }

    public <TResources, TEntityData> void render(
            final EcsRenderAdapter<TResources, TEntityData> adapter,
            final GameState state,
            final long accumulator
    ) {
        final var vertexSizeInBytes = adapter.getVertexFormat()
                                             .getInstanceSizeInBytes();
        final var buffer = getBufferFor(adapter);
        final var mesh = adapter.getMesh();
        mesh.setPointSize(5.0f);
        mesh.startDrawing();

        final var requirements = requirementsFor(adapter);

        final var preFetchedResources = state.world().fetchResources(requirements.resources().componentTypes());
        final var resources = requirements.constructResources(preFetchedResources);

        final var entities = state.world().iterateEntities(requirements.entityData().componentTypes(),
                                                           requirements.entityData().excluded(),
                                                           requirements.entityData().optional(),
                                                           requirements::constructEntityData);
        final var entityStream = StreamSupport.stream(entities, false);

        final var nQueued = new QueueCounter();
        adapter.tick(resources, entityStream, accumulator)
               .forEach(writer -> {
                   if (nQueued.value == MAX_PER_BATCH) {
                       flush(buffer, mesh, nQueued.value);
                       nQueued.value = 0;
                   }

                   final var offset = nQueued.value * vertexSizeInBytes;
                   writer.write(buffer, offset);
                   nQueued.value = nQueued.value + 1;
               });

        if (nQueued.value > 0) {
            flush(buffer, mesh, nQueued.value);
        }
    }

    @SuppressWarnings("unchecked")
    private <TResources, TEntityData> ParsedRequirements<TResources, TEntityData, EcsSystem.NoEvents> requirementsFor(
            final EcsRenderAdapter<TResources, TEntityData> adapter
    ) {
        return (ParsedRequirements<TResources, TEntityData, EcsSystem.NoEvents>) this.adapterRequirements.get(adapter.getClass());
    }

    private void flush(final ByteBuffer buffer, final Mesh mesh, final int count) {
        mesh.updateInstanceData(buffer, 0, count);
        mesh.drawInstanced(count, mesh.getIndexCount());
    }

    private ByteBuffer getBufferFor(final EcsRenderAdapter<?, ?> adapter) {
        return this.adapterBuffers.computeIfAbsent(adapter.getClass(),
                                                   ignored -> memAlloc(MAX_PER_BATCH * adapter.getVertexSizeInBytes()));
    }

    @SuppressWarnings("unchecked")
    private static <TResources, TEntityData> ParsedRequirements<TResources, TEntityData, EcsSystem.NoEvents>
    resolveRequirements(final EcsRenderAdapter<TResources, TEntityData> adapter) {
        final var interfaceType = Arrays.stream(adapter.getClass().getGenericInterfaces())
                                        .filter(type -> ParameterizedType.class.isAssignableFrom(type.getClass()))
                                        .map(ParameterizedType.class::cast)
                                        .filter(RenderDispatcherImpl::isAdapter)
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Could not find EcsRenderAdapter from adapter implements list!"));

        final var resourceDataType = interfaceType.getActualTypeArguments()[0];
        final var entityDataType = interfaceType.getActualTypeArguments()[1];

        return new ParsedRequirements<>(adapter.getClass().getSimpleName(),
                                        SystemInputRecord.Resources.createFor((Class<TResources>) resourceDataType),
                                        SystemInputRecord.EntityData.createFor((Class<TEntityData>) entityDataType),
                                        SystemInputRecord.Events.createFor(EcsSystem.NoEvents.class));
    }

    private static boolean isAdapter(final ParameterizedType type) {
        return ((Class<?>) type.getRawType()).isAssignableFrom(EcsRenderAdapter.class);
    }

    /**
     * Java specification enforces that local references used in lambdas must be effectively final. This limitation
     * prevents using raw ints as counters in loops written using <code>x.forEach(...)</code> -style calls.
     * <p>
     * To overcome this limitation, just wrap the int in an instance which may then be final, so that the reference is
     * final and incrementing the counter is just some interior mutability within an immutable reference.
     */
    private static final class QueueCounter {
        private int value;
    }
}
