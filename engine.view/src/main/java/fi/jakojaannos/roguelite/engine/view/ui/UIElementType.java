package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.view.ui.builder.*;

public final class UIElementType<TBuilder extends UIElementBuilder<TBuilder>> {
    public static final UIElementType<GenericUIElementBuilder> NONE = new UIElementType<>(GenericUIElementBuilder::new);
    public static final UIElementType<UIPanelBuilder> PANEL = new UIElementType<>(UIPanelBuilder::new);
    public static final UIElementType<UILabelBuilder> LABEL = new UIElementType<>(UILabelBuilder::new);
    public static final UIElementType<UIProgressBarBuilder> PROGRESS_BAR = new UIElementType<>(UIProgressBarBuilder::new);
    private final BuilderFactory<TBuilder> builderFactory;

    private UIElementType(final BuilderFactory<TBuilder> builderFactory) {
        this.builderFactory = builderFactory;
    }

    public TBuilder getBuilder(
            final UserInterface userInterface,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        return this.builderFactory.apply(userInterface, entity, name, componentConsumer);
    }

    public interface BuilderFactory<TBuilder> {
        TBuilder apply(
                UserInterface userInterface,
                Entity entity,
                String name,
                Consumer<Component> componentConsumer
        );
    }
}
