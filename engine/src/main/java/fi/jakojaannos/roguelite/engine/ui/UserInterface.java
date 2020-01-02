package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.ui.builder.UIBuilder;
import fi.jakojaannos.roguelite.engine.ui.builder.UIElementBuilder;
import org.joml.Vector2d;

import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface UserInterface {
    static UIBuilder builder(
            final ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilder(viewportSizeProvider, fontSizeProvider);
    }

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

    interface ViewportSizeProvider {
        int getWidthInPixels();

        int getHeightInPixels();
    }
}
