package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Consumer;

public interface UIBuilder {
    UIBuilder element(String name, Consumer<UIElementBuilder> factory);

    UserInterface build();
}
