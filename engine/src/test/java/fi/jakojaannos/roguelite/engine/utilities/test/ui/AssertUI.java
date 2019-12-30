package fi.jakojaannos.roguelite.engine.utilities.test.ui;

import fi.jakojaannos.roguelite.engine.ui.UIElement;
import fi.jakojaannos.roguelite.engine.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class AssertUI {
    public static AssertUI assertUI(final UserInterface userInterface) {
        return new AssertUI(userInterface);
    }

    private final UserInterface userInterface;

    AssertUI(final UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public UIElementMatcher hasExactlyOneElementWithName(final String name) {
        List<UIElement> elements = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, value -> value.equals(name))
                                                .collect(Collectors.toList());

        assertEquals(1, elements.size(), "Expected there to be exactly one element with name \"" + name + "\"");
        return new UIElementMatcher(elements.get(0));
    }

}
