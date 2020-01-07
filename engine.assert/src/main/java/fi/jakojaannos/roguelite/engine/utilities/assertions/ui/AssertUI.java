package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertUI {
    public static AssertUI assertUI(final UserInterface userInterface) {
        return new AssertUI(userInterface);
    }

    private final UserInterface userInterface;

    AssertUI(final UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public UIElementMatcher hasExactlyOneElementWithName(
            final String name
    ) {
        List<UIElement> elements = this.userInterface.findElementsWithMatchingProperty(UIProperty.NAME, value -> value.equals(name))
                                                     .collect(Collectors.toList());

        assertEquals(1, elements.size(), "Expected there to be exactly one element with name \"" + name + "\"");
        return new UIElementMatcher(elements.get(0), this.userInterface.getWidth(), this.userInterface.getHeight());
    }

    public UIElementMatcher hasExactlyOneElementWithMatchingChild(
            final Consumer<UIElementMatcher> matcher
    ) {
        List<UIElement> elements = this.userInterface.allElements()
                                                     .filter(element -> element.getChildren().stream().anyMatch(child -> {
                                                         try {
                                                             matcher.accept(new UIElementMatcher(child, this.userInterface.getWidth(), this.userInterface.getHeight()));
                                                             return true;
                                                         } catch (AssertionError ignored) {
                                                             return false;
                                                         }
                                                     })).collect(Collectors.toList());

        assertEquals(1, elements.size(), "Expected there to be exactly one element with matching child!");
        return new UIElementMatcher(elements.get(0), this.userInterface.getWidth(), this.userInterface.getHeight());
    }

    public void hasNoElementMatching(
            final Consumer<UIElementMatcher> matcher
    ) {
        List<UIElement> elements = this.userInterface.allElements()
                                                     .filter(element -> {
                                                         try {
                                                             matcher.accept(new UIElementMatcher(element, this.userInterface.getWidth(), this.userInterface.getHeight()));
                                                             return true;
                                                         } catch (AssertionError ignored) {
                                                             return false;
                                                         }
                                                     }).collect(Collectors.toList());

        assertEquals(0, elements.size(), "Expected there to be no matching elements!");
    }

    public void hasMatchingElements(
            final int expectedNumber,
            final Consumer<UIElementMatcher> matcher
    ) {
        List<UIElement> elements = this.userInterface.allElements()
                                                     .filter(element -> {
                                                         try {
                                                             matcher.accept(new UIElementMatcher(element, this.userInterface.getWidth(), this.userInterface.getHeight()));
                                                             return true;
                                                         } catch (AssertionError ignored) {
                                                             return false;
                                                         }
                                                     }).collect(Collectors.toList());

        assertEquals(expectedNumber, elements.size(), "Expected there to be exactly expected number of matching elements!");
    }

    public ElementStreamMatcher elementsMatching(
            // TODO: Refactor matcher to be a "builder" with separate assert() and getAsBoolean()
            final Consumer<UIElementMatcher> matcher
    ) {
        List<UIElement> elements = this.userInterface.allElements()
                                                     .filter(element -> {
                                                         try {
                                                             matcher.accept(new UIElementMatcher(element, this.userInterface.getWidth(), this.userInterface.getHeight()));
                                                             return true;
                                                         } catch (AssertionError ignored) {
                                                             return false;
                                                         }
                                                     }).collect(Collectors.toList());
        return new ElementStreamMatcher(elements.stream(), this.userInterface);
    }
}
