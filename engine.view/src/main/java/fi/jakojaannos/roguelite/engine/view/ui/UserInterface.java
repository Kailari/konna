package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Consumer;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.EventSender;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;

public interface UserInterface {
    int getHeight();

    int getWidth();

    Stream<UIElement> getRoots();

    static UIBuilder builder(
            final Events events,
            final TimeManager timeManager,
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilder(events, timeManager, viewport, fontSizeProvider);
    }

    void update(Mouse mouse);

    default Stream<UIElement> findElements(final Consumer<UIElementMatcher> builder) {
        final var matcher = UIElementMatcher.create();
        builder.accept(matcher);

        return getRoots().flatMap(root -> matchingElements(root, matcher));
    }

    <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            String name,
            T elementType,
            Consumer<TBuilder> factory
    );

    private Stream<UIElement> matchingElements(
            final UIElement element,
            final UIElementMatcher matcher
    ) {
        final var matchingChildren = element.getChildren()
                                            .stream()
                                            .flatMap(child -> matchingElements(child, matcher));
        if (matcher.evaluate(element)) {
            return Stream.concat(Stream.of(element), matchingChildren);
        }

        return matchingChildren;
    }

    UIRoot getRoot();

    interface UIEventBus extends EventSender<UIEvent> {}
}
