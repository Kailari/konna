package fi.jakojaannos.konna.engine.view.dispatcher;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.RenderDispatcher;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.ParsedRequirements;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.SystemInputRecord;

@SuppressWarnings("rawtypes")
public class RenderDispatcherImpl implements RenderDispatcher {
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
    public void dispatch(
            final Renderer renderer,
            final GameState state,
            final long accumulator
    ) {
        for (final var adapter : this.adapters) {
            dispatch(renderer, adapter, state, accumulator);
        }
    }

    public <TResources, TEntityData> void dispatch(
            final Renderer renderer,
            final EcsRenderAdapter<TResources, TEntityData> adapter,
            final GameState state,
            final long accumulator
    ) {
        final var requirements = requirementsFor(adapter);

        final var preFetchedResources = state.world().fetchResources(requirements.resources().componentTypes());
        final var resources = requirements.constructResources(preFetchedResources);

        final var entities = state.world().iterateEntities(requirements.entityData().componentTypes(),
                                                           requirements.entityData().excluded(),
                                                           requirements.entityData().optional(),
                                                           requirements::constructEntityData,
                                                           false);

        adapter.draw(renderer, resources, entities, accumulator);
    }

    @SuppressWarnings("unchecked")
    private <TResources, TEntityData> ParsedRequirements<TResources, TEntityData, EcsSystem.NoEvents> requirementsFor(
            final EcsRenderAdapter<TResources, TEntityData> adapter
    ) {
        return (ParsedRequirements<TResources, TEntityData, EcsSystem.NoEvents>) this.adapterRequirements.get(adapter.getClass());
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
}
