package fi.jakojaannos.roguelite.engine.view.ui;

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
            final UIElement element,
            final String name
    ) {
        return this.builderFactory.apply(userInterface, element, name);
    }

    public interface BuilderFactory<TBuilder> {
        TBuilder apply(
                UserInterface userInterface,
                UIElement element,
                String name
        );
    }
}
