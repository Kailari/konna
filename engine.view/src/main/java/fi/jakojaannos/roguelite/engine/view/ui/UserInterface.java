package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;
import org.joml.Vector2d;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface UserInterface {
    static UIBuilder builder(
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilder(viewport, fontSizeProvider);
    }

    int getHeight();

    int getWidth();

    Queue<UIEvent> update(Vector2d mousePos, boolean mouseClicked);

    Stream<UIElement> getRoots();

    default <T> Stream<UIElement> findElementsWithMatchingProperty(
            final UIProperty<T> property,
            final Predicate<T> matcher
    ) {
        Stream.Builder<UIElement> streamBuilder = Stream.builder();
        this.getRoots().forEach(root -> addIfMatching(property, matcher, streamBuilder, root));
        return streamBuilder.build();
    }

    private <T> void addIfMatching(
            final UIProperty<T> property,
            final Predicate<T> matcher,
            final Stream.Builder<UIElement> streamBuilder,
            final UIElement element
    ) {
        element.getProperty(property)
               .filter(matcher)
               .ifPresent(ignored -> streamBuilder.add(element));

        element.getChildren().forEach(child -> addIfMatching(property, matcher, streamBuilder, child));
    }

    <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    );
}
