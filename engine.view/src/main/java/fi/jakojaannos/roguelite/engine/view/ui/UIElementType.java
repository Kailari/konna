package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIElementType<TBuilder extends UIElementBuilder<TBuilder>> {
    public static final UIElementType<UIPanelBuilder> PANEL = new UIElementType<>(UIPanelBuilder::new);
    public static final UIElementType<UILabelBuilder> LABEL = new UIElementType<>(UILabelBuilder::new);

    private final BiFunction<String, Consumer<Component>, TBuilder> builderFactory;

    public TBuilder getBuilder(
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        return this.builderFactory.apply(name, componentConsumer);
    }
}
