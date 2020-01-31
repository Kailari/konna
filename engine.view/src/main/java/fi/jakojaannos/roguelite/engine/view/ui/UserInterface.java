package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIPropertyMatcher;

public interface UserInterface {
    static UIBuilder builder(
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilder(viewport, fontSizeProvider);
    }

    int getHeight();

    int getWidth();

    void update(TimeManager time, Mouse mouse, Events events);

    Stream<UIElement> getRoots();

    Stream<UIElement> allElements();

    /**
     * @deprecated Use {@link #findElements(Consumer)} with {@link UIPropertyMatcher} static methods instead
     */
    @Deprecated
    default <T> Stream<UIElement> findElementsWithMatchingProperty(
            final UIProperty<T> property,
            final Predicate<T> matcher
    ) {
        final Stream.Builder<UIElement> streamBuilder = Stream.builder();
        final var elementMatcher = UIElementMatcher.create();
        elementMatcher.matching(UIPropertyMatcher.match(property)
                                                 .isPresentAndMatches(matcher));

        this.getRoots().forEach(root -> addIfMatching(elementMatcher, streamBuilder, root));
        return streamBuilder.build();
    }

    default Stream<UIElement> findElements(final Consumer<UIElementMatcher> builder) {
        final var matcher = UIElementMatcher.create();
        builder.accept(matcher);

        final var streamBuilder = Stream.<UIElement>builder();
        getRoots().forEach(root -> addIfMatching(matcher, streamBuilder, root));
        return streamBuilder.build();
    }

    <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            String name,
            T elementType,
            Consumer<TBuilder> factory
    );

    private void addIfMatching(
            final UIElementMatcher matcher,
            final Stream.Builder<UIElement> streamBuilder,
            final UIElement element
    ) {
        if (matcher.evaluate(element)) {
            streamBuilder.add(element);
        }

        element.getChildren().forEach(child -> addIfMatching(matcher, streamBuilder, child));
    }
}
