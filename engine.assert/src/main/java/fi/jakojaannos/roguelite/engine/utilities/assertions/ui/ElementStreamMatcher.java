package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ElementStreamMatcher {
    private final Stream<UIElement> stream;
    private final UserInterface userInterface;

    public ElementStreamMatcher(final Stream<UIElement> stream, final UserInterface userInterface) {
        this.stream = stream;
        this.userInterface = userInterface;
    }

    public void allMatch(final Predicate<UIElementMatcher> matcher) {
        assertTrue(this.stream.map(element -> new UIElementMatcher(element, this.userInterface.getWidth(), this.userInterface.getHeight()))
                              .allMatch(matcher));
    }

    public void anyMatch(final Predicate<UIElementMatcher> matcher) {
        assertTrue(this.stream.map(element -> new UIElementMatcher(element, this.userInterface.getWidth(), this.userInterface.getHeight()))
                              .anyMatch(matcher));
    }
}
