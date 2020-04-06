package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.legacy.*;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.AutomaticSizeTag;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.BoundHeight;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.BoundWidth;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.FontSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public class UILabelAutomaticSizeCalculationSystem implements ECSSystem {
    private final TextSizeProvider font;

    public UILabelAutomaticSizeCalculationSystem(final TextSizeProvider font) {
        this.font = font;
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
                                                      .map(parentEntity -> getFontSize(entityManager,
                                                                                       parentEntity,
                                                                                       hierarchy,
                                                                                       uiRoot))
                                                      .orElseGet(uiRoot::getFontSize));
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .tickBefore(UIElementBoundaryCalculationSystem.class)
                    .requireProvidedResource(UIHierarchy.class)
                    .requireProvidedResource(UIRoot.class)
                    .withComponent(AutomaticSizeTag.class)
                    .withComponent(Text.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var hierarchy = world.getResource(UIHierarchy.class);
        final var uiRoot = world.getResource(UIRoot.class);

        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            final var fontSize = getFontSize(entityManager, entity, hierarchy, uiRoot);
            final var textComponent = entityManager.getComponentOf(entity, Text.class)
                                                   .orElseThrow();
            final var text = textComponent.getText();
            final var font = this.font; // TODO: Get font from hierarchy like the font size is get

            entityManager.addComponentIfAbsent(entity, BoundWidth.class,
                                               () -> new BoundWidth(ProportionValue.absolute(0)));
            entityManager.addComponentIfAbsent(entity, BoundHeight.class,
                                               () -> new BoundHeight(ProportionValue.absolute(0)));

            final int width = (int) font.getStringWidthInPixels(fontSize, text);
            final var widthBound = entityManager.getComponentOf(entity, BoundWidth.class).orElseThrow();
            widthBound.value = ProportionValue.absolute(width);

            final int height = (int) font.getStringHeightInPixels(fontSize, text);
            final var heightBound = entityManager.getComponentOf(entity, BoundHeight.class).orElseThrow();
            heightBound.value = ProportionValue.absolute(height);
        });
    }
}
