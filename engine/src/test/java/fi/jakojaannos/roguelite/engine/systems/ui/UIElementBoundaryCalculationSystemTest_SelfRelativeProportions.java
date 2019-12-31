package fi.jakojaannos.roguelite.engine.systems.ui;

import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.ui.builder.UIBuilder;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fi.jakojaannos.roguelite.engine.ui.ProportionValue.absolute;
import static fi.jakojaannos.roguelite.engine.ui.ProportionValue.percentOf;
import static fi.jakojaannos.roguelite.engine.utilities.test.ui.AssertUI.assertUI;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UIElementBoundaryCalculationSystemTest_SelfRelativeProportions {
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    private UIBuilder uiBuilder;

    @BeforeEach
    void beforeEach() {
        uiBuilder = UserInterface.builder(new UserInterface.ViewportSizeProvider() {
            @Override
            public int getWidthInPixels() {
                return VIEWPORT_WIDTH;
            }

            @Override
            public int getHeightInPixels() {
                return VIEWPORT_HEIGHT;
            }
        }, (fontSize, text) -> fontSize / 1.5 * text.length());
    }

    @Test
    void buildingUIElementWithWidthRelativeToSelfThrows() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownWidth(0.1)))
                .build();

        assertThrows(IllegalStateException.class, () -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithHeightRelativeToSelfThrows() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(percentOf().ownHeight(0.1)))
                .build();

        assertThrows(IllegalStateException.class, () -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithWidthAndHeightRelativeToEachOtherThrows() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .height(percentOf().ownWidth(0.1)))
                .build();

        assertThrows(IllegalStateException.class, () -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithWidthRelativeToHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .height(absolute(100)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithWidthRelativeToDefaultHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithWidthRelativeToCalculatedHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .top(absolute(10))
                                           .bottom(absolute(20)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithHeightRelativeToWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(100))
                                           .height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithHeightRelativeToDefaultWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithHeightRelativeToCalculatedWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .right(absolute(20))
                                           .height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnWidthThrows() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().ownWidth(0.1))
                                           .right(percentOf().ownWidth(0.05)))
                .build();

        assertThrows(IllegalStateException.class, () -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithLeftRelativeToOwnWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(200))
                                           .left(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithRightRelativeToOwnWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(200))
                                           .right(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithTopRelativeToOwnHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .top(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithBottomRelativeToOwnHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .bottom(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnHeightWithoutSettingHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().ownHeight(0.1))
                                           .right(percentOf().ownHeight(0.05)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Vector2d(0), false));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .left(percentOf().ownHeight(0.25))
                                           .right(percentOf().ownHeight(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(50)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH - 20)
                .with(UIProperty.WIDTH).equalTo(VIEWPORT_WIDTH - (50 + 20));
    }
}
