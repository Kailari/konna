package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.events.ActiveTag;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import lombok.val;

import java.util.stream.Stream;

public class UIElementHoverEventProvider implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.EVENTS)
                    .requireResource(Events.class)
                    .requireResource(Mouse.class)
                    .withComponent(Name.class)
                    .withComponent(ElementBoundaries.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val events = world.getOrCreateResource(Events.class);
        val mouse = world.getOrCreateResource(Mouse.class);

        entities.forEach(entity -> {
            val name = entityManager.getComponentOf(entity, Name.class)
                                    .map(component -> component.value)
                                    .orElseThrow();
            val bounds = entityManager.getComponentOf(entity, ElementBoundaries.class).orElseThrow();
            if (mouse.position.x > bounds.minX && mouse.position.x < bounds.maxX && mouse.position.y > bounds.minY && mouse.position.y < bounds.maxY) {
                if (!entityManager.hasComponent(entity, ActiveTag.class)) {
                    events.getUi().fire(new UIEvent(name, UIEvent.Type.START_HOVER));
                    entityManager.addComponentTo(entity, new ActiveTag());
                }
            } else {
                if (entityManager.hasComponent(entity, ActiveTag.class)) {
                    events.getUi().fire(new UIEvent(name, UIEvent.Type.END_HOVER));
                    entityManager.removeComponentFrom(entity, new ActiveTag());
                }
            }
        });
    }
}
