package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIElementType<TBuilder extends UIElementBuilder<TBuilder>> {
    public static final UIElementType<UIPanelBuilder> PANEL = new UIElementType<>(UIPanelBuilder::new);
    public static final UIElementType<UILabelBuilder> LABEL = new UIElementType<>(UILabelBuilder::new);

    private final BuilderFactory<TBuilder> builderFactory;

    public TBuilder getBuilder(
            final UIBuilder uiBuilder,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        return this.builderFactory.apply(uiBuilder, entity, name, componentConsumer);
    }

    public interface BuilderFactory<TBuilder> {
        TBuilder apply(
                UIBuilder uiBuilder,
                Entity entity,
                String name,
                Consumer<Component> componentConsumer
        );
    }
}
