package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.FontSize;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.data.resources.RenderPass;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIRoot;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class UILabelRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.RENDERING)
                    .withComponent(ElementBoundaries.class)
                    .withComponent(Text.class);
    }

    private final TextRenderer textRenderer;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val hierarchy = world.getOrCreateResource(UIHierarchy.class);
        val uiRoot = world.getOrCreateResource(UIRoot.class);
        val renderPass = world.getOrCreateResource(RenderPass.class);

        val entityManager = world.getEntityManager();
        entities.filter(entity -> hierarchy.getLevelOf(entity) == renderPass.value)
                .forEach(entity -> {
                    val fontSize = getFontSize(entityManager, entity, hierarchy, uiRoot);
                    val text = entityManager.getComponentOf(entity, Text.class)
                                            .orElseThrow().text;
                    val bounds = entityManager.getComponentOf(entity, ElementBoundaries.class).orElseThrow();

                    val x = bounds.minX;
                    val y = bounds.minY;
                    this.textRenderer.drawOnScreen(x, y, fontSize, text);
                });
    }

    private static int getFontSize(
            final EntityManager entityManager,
            final Entity entity,
            final UIHierarchy hierarchy,
            final UIRoot uiRoot
    ) {
        return entityManager.getComponentOf(entity, FontSize.class)
                            .map(fontSize -> fontSize.value)
                            .orElseGet(() -> hierarchy.getParentOf(entity)
                                                      .map(parentEntity -> getFontSize(entityManager, parentEntity, hierarchy, uiRoot))
                                                      .orElseGet(uiRoot::getFontSize));
    }
}
