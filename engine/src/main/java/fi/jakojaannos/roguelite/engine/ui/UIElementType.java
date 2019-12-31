package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ui.builder.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.ui.builder.UILabelBuilder;
import fi.jakojaannos.roguelite.engine.ui.builder.UIPanelBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class UIElementType<TBuilder extends UIElementBuilder<TBuilder>> {
    public static final UIElementType<UIPanelBuilder> PANEL = new UIElementType<>(UIPanelBuilder::new);
    public static final UIElementType<UILabelBuilder> LABEL = new UIElementType<>(UILabelBuilder::new);

    private final BuilderFactory<TBuilder> builderFactory;

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
