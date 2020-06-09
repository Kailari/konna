package fi.jakojaannos.roguelite.engine.view.ui;

import java.util.function.Consumer;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.EventSender;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilderImpl;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;

public interface UserInterface {
    int getHeight();

    int getWidth();

    Stream<UIElement> getRoots();

    UIRoot getRoot();

    static UIBuilder builder(
            final Events events,
            final TimeManager timeManager,
            final Viewport viewport,
            final TextSizeProvider fontSizeProvider
    ) {
        return new UIBuilderImpl(events, timeManager, viewport, fontSizeProvider);
    }

    void update(Mouse mouse);

    default Stream<UIElement> findElements(final Consumer<UIElementMatcher> builder) {
        final var matcher = UIElementMatcher.create();
        builder.accept(matcher);

        return getRoots().flatMap(root -> matchingElements(root, matcher));
    }

    UIElement addElement(String name, Consumer<UIElementBuilder> factory);

    private static Stream<UIElement> matchingElements(
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

    interface UIEventBus extends EventSender<UIEvent> {}
}
