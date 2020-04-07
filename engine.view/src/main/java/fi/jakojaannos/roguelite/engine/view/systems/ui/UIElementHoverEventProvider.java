package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.events.ActiveTag;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;

public class UIElementHoverEventProvider implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.EVENTS)
                    .requireProvidedResource(Events.class)
                    .requireResource(Mouse.class)
                    .withComponent(Name.class)
                    .withComponent(ElementBoundaries.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var events = world.fetchResource(Events.class);
        final var mouse = world.fetchResource(Mouse.class);

        entities.forEach(entity -> {
            final var name = entityManager.getComponentOf(entity, Name.class)
                                          .map(component -> component.value)
                                          .orElseThrow();
            final var bounds = entityManager.getComponentOf(entity, ElementBoundaries.class).orElseThrow();
            if (isInside(mouse, bounds)) {
                if (!entityManager.hasComponent(entity, ActiveTag.class)) {
                    events.ui().fire(new UIEvent(name, UIEvent.Type.START_HOVER));
                    entityManager.addComponentTo(entity, new ActiveTag());
                }
            } else {
                if (entityManager.hasComponent(entity, ActiveTag.class)) {
                    events.ui().fire(new UIEvent(name, UIEvent.Type.END_HOVER));
                    entityManager.removeComponentFrom(entity, new ActiveTag());
                }
            }
        });
    }

    private boolean isInside(final Mouse mouse, final ElementBoundaries bounds) {
        return mouse.position.x > bounds.minX && mouse.position.x < bounds.maxX
                && mouse.position.y > bounds.minY && mouse.position.y < bounds.maxY;
    }
}
