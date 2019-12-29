package fi.jakojaannos.roguelite.engine.systems.ui;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.Parent;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.ecs.*;
import lombok.val;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Keeps the UI hierarchy up to date.
 */
public class UIHierarchySystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .requireResource(UIHierarchy.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val hierarchy = world.getOrCreateResource(UIHierarchy.class);
        val entityManager = world.getEntityManager();
        hierarchy.clear();
        entities.forEach(entity -> hierarchy.update(entityManager,
                                                    entity,
                                                    getParent(entityManager, entity)));
    }

    @Nullable
    private static Entity getParent(final EntityManager entityManager, final Entity entity) {
        return entityManager.getComponentOf(entity, Parent.class)
                            .map(parent -> parent.value)
                            .orElse(null);
    }
}
