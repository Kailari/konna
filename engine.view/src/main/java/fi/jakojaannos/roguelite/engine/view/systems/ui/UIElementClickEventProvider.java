package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.events.ActiveTag;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.events.ClickTimestamp;
import lombok.val;

import java.util.stream.Stream;

public class UIElementClickEventProvider implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.EVENTS)
                    .requireResource(Events.class)
                    .requireResource(Mouse.class)
                    .requireResource(Time.class)
                    .tickAfter(UIElementHoverEventProvider.class)
                    .withComponent(Name.class)
                    .withComponent(ActiveTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val events = world.getOrCreateResource(Events.class);
        val mouse = world.getOrCreateResource(Mouse.class);
        val timeManager = world.getOrCreateResource(Time.class);

        entities.forEach(entity -> {
            val name = entityManager.getComponentOf(entity, Name.class)
                                    .map(component -> component.value)
                                    .orElseThrow();
            if (mouse.clicked) {
                val clicked = entityManager.addComponentIfAbsent(entity, ClickTimestamp.class, ClickTimestamp::new);
                if (clicked.releasedSince) {
                    clicked.releasedSince = false;
                    clicked.timestamp = timeManager.getCurrentGameTime();
                    events.getUi().fire(new UIEvent(name, UIEvent.Type.CLICK));
                }
            } else if (entityManager.hasComponent(entity, ClickTimestamp.class)) {
                val clicked = entityManager.getComponentOf(entity, ClickTimestamp.class).orElseThrow();
                clicked.releasedSince = true;
            }
        });
    }
}
