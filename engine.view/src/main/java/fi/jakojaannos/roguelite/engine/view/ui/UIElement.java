package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;

public interface UIElement {
    /**
     * Gets the parent of this element.
     *
     * @return the parent element. <code>Optional.empty()</code> if this element is at the root level
     */
    Optional<UIElement> getParent();

    /**
     * Gets all children of this element. The collection is unmodifiable.
     *
     * @return children as a collection, empty collection if there are no children
     */
    Collection<UIElement> getChildren();

    /**
     * Sets the value of a property.
     *
     * @param property property to modify
     * @param value    the new value
     * @param <T>      the type of the property value
     */
    default <T> void setProperty(final UIProperty<T> property, final T value) {
        property.set(this, value);
    }

    /**
     * Gets the value of a property
     *
     * @param property property to query
     * @param <T>      the type of the property value
     *
     * @return current value or <code>Optional.empty()</code> if the property is not present
     */
    default <T> Optional<T> getProperty(final UIProperty<T> property) {
        return property.getFor(this);
    }

    /**
     * Queries the direct children of this element for matching components
     *
     * @param builder builder for constructing the matcher
     *
     * @return stream containing all matching direct child elements
     */
    default Stream<UIElement> findChildren(final Consumer<UIElementMatcher> builder) {
        final var matcher = UIElementMatcher.create();
        builder.accept(matcher);

        final Stream.Builder<UIElement> matchingStreamBuilder = Stream.builder();
        getChildren().stream()
                     .filter(matcher::evaluate)
                     .forEach(matchingStreamBuilder);

        return matchingStreamBuilder.build();
    }

    /**
     * Queries the UI hierarchy for matching nodes, starting from this element's children.
     *
     * @param builder builder for constructing the matcher
     *
     * @return stream containing all matching elements from the hierarchy starting at this element
     */
    default Stream<UIElement> findInChildren(final Consumer<UIElementMatcher> builder) {
        final var matcher = UIElementMatcher.create();
        builder.accept(matcher);

        final Stream.Builder<UIElement> matchingStreamBuilder = Stream.builder();
        final Queue<UIElement> queue = new ArrayDeque<>(getChildren());
        while (!queue.isEmpty()) {
            final var element = queue.poll();

            if (matcher.evaluate(element)) {
                matchingStreamBuilder.accept(element);
            }

            queue.addAll(element.getChildren());
        }

        return matchingStreamBuilder.build();
    }
}
