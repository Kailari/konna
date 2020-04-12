package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.Requirements;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.AutomaticSizeTag;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.BoundHeight;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.BoundWidth;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.FontSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public class UILabelAutomaticSizeCalculationSystem implements EcsSystem<UILabelAutomaticSizeCalculationSystem.Resources, UILabelAutomaticSizeCalculationSystem.EntityData, EcsSystem.NoEvents> {
    private final TextSizeProvider font;

    // FIXME: Read font from UI
    public UILabelAutomaticSizeCalculationSystem(final TextSizeProvider font) {
        this.font = font;
    }

    @Override
    public Requirements<Resources, EntityData, NoEvents> declareRequirements(
            final Requirements<Resources, EntityData, NoEvents> require
    ) {
        return require.resources(Resources.class)
                      .entityData(EntityData.class);
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var hierarchy = resources.hierarchy;
        final var uiRoot = resources.uiRoot;

        entities.forEach(entity -> {
            final var fontSize = getFontSize(entity.getHandle(), hierarchy, uiRoot);
            final var textComponent = entity.getComponent(Text.class).orElseThrow();
            final var text = textComponent.getText();
            final var font = this.font; // TODO: Get font from hierarchy like the font size is get

            entity.addOrGet(BoundWidth.class, () -> new BoundWidth(ProportionValue.absolute(0)));
            entity.addOrGet(BoundHeight.class, () -> new BoundHeight(ProportionValue.absolute(0)));

            final int width = (int) font.getStringWidthInPixels(fontSize, text);
            final var widthBound = entity.getComponent(BoundWidth.class).orElseThrow();
            widthBound.value = ProportionValue.absolute(width);

            final int height = (int) font.getStringHeightInPixels(fontSize, text);
            final var heightBound = entity.getComponent(BoundHeight.class).orElseThrow();
            heightBound.value = ProportionValue.absolute(height);
        });
    }

    private static int getFontSize(
            final EntityHandle entity,
            final UIHierarchy hierarchy,
            final UIRoot uiRoot
    ) {
        return entity.getComponent(FontSize.class)
                     .map(fontSize -> fontSize.value)
                     .orElseGet(() -> hierarchy.getParentOf(entity)
                                               .map(parentEntity -> getFontSize(parentEntity,
                                                                                hierarchy,
                                                                                uiRoot))
                                               .orElseGet(uiRoot::getFontSize));
    }

    public static record Resources(
            UIHierarchy hierarchy,
            UIRoot uiRoot
    ) {
    }

    public static record EntityData(
            AutomaticSizeTag autoTag,
            Text text
    ) {
    }
}
