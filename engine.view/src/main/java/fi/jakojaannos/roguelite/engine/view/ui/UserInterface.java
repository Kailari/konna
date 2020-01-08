package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;
import lombok.val;

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

    void update(final TimeManager time, final Mouse mouse, final Events events);

    Stream<UIElement> getRoots();

    Stream<UIElement> allElements();

    default <T> Stream<UIElement> findElementsWithMatchingProperty(
            final UIProperty<T> property,
            final Predicate<T> matcher
    ) {
        Stream.Builder<UIElement> streamBuilder = Stream.builder();
        this.getRoots().forEach(root -> addIfMatching(property, matcher, streamBuilder, root));
        return streamBuilder.build();
    }

    default <T> Stream<UIElement> findElements(
            final Consumer<UIElementMatcher> builder
    ) {
        Stream.Builder<UIElement> streamBuilder = Stream.builder();
        this.getRoots().forEach(root -> addIfMatching(builder, streamBuilder, root));
        return streamBuilder.build();
    }

    <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    );


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

    private void addIfMatching(
            final Consumer<UIElementMatcher> builder,
            final Stream.Builder<UIElement> streamBuilder,
            final UIElement element
    ) {
        val elementMatcher = UIElementMatcher.create();
        builder.accept(elementMatcher);
        if (elementMatcher.evaluate(element)) {
            streamBuilder.add(element);
        }

        element.getChildren().forEach(child -> addIfMatching(builder, streamBuilder, child));
    }
}
