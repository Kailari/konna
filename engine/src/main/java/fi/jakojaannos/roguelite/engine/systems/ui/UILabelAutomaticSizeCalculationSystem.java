package fi.jakojaannos.roguelite.engine.systems.ui;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.AutomaticSizeTag;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.BoundHeight;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.BoundWidth;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.FontSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.label.Text;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class UILabelAutomaticSizeCalculationSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .tickBefore(UIElementBoundaryCalculationSystem.class)
                    .requireResource(UIHierarchy.class)
                    .requireResource(UIRoot.class)
                    .withComponent(AutomaticSizeTag.class)
                    .withComponent(Text.class);
    }

    private final TextSizeProvider font;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val hierarchy = world.getOrCreateResource(UIHierarchy.class);
        val uiRoot = world.getOrCreateResource(UIRoot.class);

        val entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            val fontSize = getFontSize(entityManager, entity, hierarchy, uiRoot);
            val textComponent = entityManager.getComponentOf(entity, Text.class)
                                             .orElseThrow();
            val text = textComponent.text;
            val font = this.font; // TODO: Get font from hierarchy like the font size is get

            entityManager.addComponentIfAbsent(entity, new BoundWidth(ProportionValue.absolute(0)));
            entityManager.addComponentIfAbsent(entity, new BoundHeight(ProportionValue.absolute(0)));

            int width = (int) font.getStringWidthInPixels(fontSize, text);
            val widthBound = entityManager.getComponentOf(entity, BoundWidth.class).orElseThrow();
            widthBound.value = ProportionValue.absolute(width);

            int height = (int) font.getStringHeightInPixels(fontSize, text);
            val heightBound = entityManager.getComponentOf(entity, BoundHeight.class).orElseThrow();
            heightBound.value = ProportionValue.absolute(height);
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
