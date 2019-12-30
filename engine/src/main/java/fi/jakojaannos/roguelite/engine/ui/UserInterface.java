package fi.jakojaannos.roguelite.engine.ui;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ui.builder.UIBuilder;
import org.joml.Vector2d;

import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface UserInterface extends Resource {
    static UIBuilder builder(
            final ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilder(viewportSizeProvider, fontSizeProvider);
    }

    Queue<UIEvent> update(Vector2d mousePos, boolean mouseClicked);

    Stream<UIElement> getRoots();

    ViewportSizeProvider getViewportSizeProvider();

    TextSizeProvider getTextSizeProvider();

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

    interface ViewportSizeProvider {
        int getWidthInPixels();

        int getHeightInPixels();
    }
}
