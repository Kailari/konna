package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.events.ActiveTag;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.events.ClickTimestamp;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UIElementClickEventProvider implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.EVENTS)
                    .requireProvidedResource(Events.class)
                    .requireResource(Mouse.class)
                    .requireProvidedResource(TimeManager.class)
                    .tickAfter(UIElementHoverEventProvider.class)
                    .withComponent(Name.class)
                    .withComponent(ActiveTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var eventBus = world.fetchResource(UserInterface.UIEventBus.class);
        final var mouse = world.fetchResource(Mouse.class);
        final var timeManager = world.fetchResource(TimeManager.class);

        entities.forEach(entity -> {
            final var name = entityManager.getComponentOf(entity, Name.class)
                                          .map(component -> component.value)
                                          .orElseThrow();

            // TODO: Entities without required component end up here!
            //      -> archetype swap is not completed correctly?
            //      -> archetype swap does its thing to wrong direction?
            //      -> spliterator does something wacky once elements get removed?
            //      -> Archetype::matchesRequirements returns false-positives?
            //      -> Components are not null'd and count is not decremented?
            final var isActive = entity.asHandle()
                                       .hasComponent(ActiveTag.class);
            if (!isActive) {
                throw new IllegalStateException("Component missing!");
            }

            if (mouse.clicked) {
                final var clicked = entityManager.addComponentIfAbsent(entity,
                                                                       ClickTimestamp.class,
                                                                       ClickTimestamp::new);
                if (clicked.releasedSince) {
                    clicked.releasedSince = false;
                    clicked.timestamp = timeManager.getCurrentGameTime();
                    eventBus.fire(new UIEvent(name, UIEvent.Type.CLICK));
                }
            } else if (entityManager.hasComponent(entity, ClickTimestamp.class)) {
                final var clicked = entityManager.getComponentOf(entity, ClickTimestamp.class).orElseThrow();
                clicked.releasedSince = true;
            }
        });
    }
}
