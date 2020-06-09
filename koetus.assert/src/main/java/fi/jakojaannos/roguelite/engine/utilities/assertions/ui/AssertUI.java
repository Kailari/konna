package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.query.UIElementMatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertUI {
    private final UserInterface userInterface;

    AssertUI(final UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public static AssertUI assertUI(final UserInterface userInterface) {
        return new AssertUI(userInterface);
    }

    public void hasExactlyOneElement(
            final Consumer<UIElementMatcher> matcher
    ) {
        final var elements = this.userInterface.findElements(matcher)
                                               .collect(Collectors.toList());

        assertEquals(1, elements.size(), "Expected there to be exactly one matching element.");
    }

    public void hasElements(
            final int n,
            final Consumer<UIElementMatcher> matcher
    ) {
        final var elements = this.userInterface.findElements(matcher)
                                               .collect(Collectors.toList());

        assertEquals(n, elements.size(), "Expected there to be " + n + " matching elements.");
    }

    public void hasNoElementMatching(
            final Consumer<UIElementMatcher> matcher
    ) {
        final var elements = this.userInterface.findElements(matcher)
                                               .collect(Collectors.toList());

        assertEquals(0, elements.size(), "Expected there to be no matching elements.");
    }
}
