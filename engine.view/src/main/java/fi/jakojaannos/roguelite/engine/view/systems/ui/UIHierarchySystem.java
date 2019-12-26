package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.Parent;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIHierarchy;
import lombok.val;

import java.util.stream.Stream;

/**
 * Keeps the UI hierarchy up to date.
 */
public class UIHierarchySystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .requireResource(UIHierarchy.class)
                    .withComponent(Parent.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val hierarchy = world.getOrCreateResource(UIHierarchy.class);
        hierarchy.clear();
        entities.forEach(entity -> hierarchy.setParent(entity, getParent(world, entity)));
    }

    private static Entity getParent(final World world, final Entity entity) {
        return world.getEntityManager()
                    .getComponentOf(entity, Parent.class)
                    .map(parent -> parent.value)
                    .orElseThrow();
    }
}
