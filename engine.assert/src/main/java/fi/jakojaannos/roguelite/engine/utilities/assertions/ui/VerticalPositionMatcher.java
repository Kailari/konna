package fi.jakojaannos.roguelite.engine.utilities.assertions.ui;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class VerticalPositionMatcher<TParent extends UIElementMatcher> {
    private final TParent parent;
    private final String name;
    private final int minY;
    private final int maxY;
    private final int height;
    private final int uiHeight;

    public TParent top() {
        val sectionMinY = 0;
        val sectionMaxY = this.uiHeight * (1.0 / 3.0);
        val elementMiddleY = this.minY + this.height / 2.0;

        assertAll(
                () -> assertTrue(elementMiddleY >= sectionMinY,
                                 String.format("Expected element \"%s\" to lie vertically within the top portion of the screen! Now it was above!",
                                               this.name)),
                () -> assertTrue(elementMiddleY <= sectionMaxY,
                                 String.format("Expected element \"%s\" to lie vertically within the top portion of the screen! Now it was below!",
                                               this.name))
        );

        return this.parent;
    }
}
