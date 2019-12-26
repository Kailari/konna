package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.panel.PanelSprite;

import java.util.function.Consumer;

public final class UIPanelBuilder extends UIElementBuilder<UIPanelBuilder> {
    public UIPanelBuilder(final String name, final Consumer<Component> componentConsumer) {
        super(name, componentConsumer);
    }

    public UIPanelBuilder borderSize(final int value) {
        this.componentConsumer.accept(new BorderSize(value));
        return this;
    }

    public UIPanelBuilder sprite(final String sprite) {
        this.componentConsumer.accept(new PanelSprite(sprite));
        return this;
    }
}
