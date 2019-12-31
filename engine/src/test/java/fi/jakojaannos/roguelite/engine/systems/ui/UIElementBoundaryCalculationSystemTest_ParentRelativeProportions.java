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

public class UIElementBoundaryCalculationSystemTest_ParentRelativeProportions {
    private static final int VIEWPORT_WIDTH = 200;
    private static final int VIEWPORT_HEIGHT = 100;

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
    void buildingUIElementWithLeftAndRightRelativeToParentWidthCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().parentWidth(0.25))
                                           .right(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(50)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH - 20)
                .with(UIProperty.WIDTH).equalTo(VIEWPORT_WIDTH - (50 + 20));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToParentHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().parentHeight(0.25))
                                           .right(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(25)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH - 10)
                .with(UIProperty.WIDTH).equalTo(VIEWPORT_WIDTH - (25 + 10));
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentWidthCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(percentOf().parentWidth(0.25))
                                           .bottom(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(50)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT - 20)
                .with(UIProperty.HEIGHT).equalTo(VIEWPORT_HEIGHT - (50 + 20));
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(percentOf().parentHeight(0.25))
                                           .bottom(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(25)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT - 10)
                .with(UIProperty.HEIGHT).equalTo(VIEWPORT_HEIGHT - (25 + 10));
    }

    @Test
    void buildingUIElementWithWidthRelativeToParentWidthCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .width(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(10)
                .with(UIProperty.MAX_X).equalTo(10 + 20)
                .with(UIProperty.WIDTH).equalTo(20);
    }

    @Test
    void buildingUIElementWithWidthRelativeToParentHeightCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .width(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(10)
                .with(UIProperty.MAX_X).equalTo(10 + 10)
                .with(UIProperty.WIDTH).equalTo(10);
    }

    @Test
    void buildingUIElementWithHeightRelativeToParentWidthCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(10))
                                           .height(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(10)
                .with(UIProperty.MAX_Y).equalTo(10 + 20)
                .with(UIProperty.HEIGHT).equalTo(20);
    }

    @Test
    void buildingUIElementWithHeightRelativeToParentHeightCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(10))
                                           .height(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(10)
                .with(UIProperty.MAX_Y).equalTo(10 + 10)
                .with(UIProperty.HEIGHT).equalTo(10);
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToParentWidthAndAnchorXSetProportionalToParentWidthCalculatesCorrectBounds() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(percentOf().parentWidth(0.25))
                                           .left(percentOf().parentWidth(0.1))
                                           .right(percentOf().parentWidth(0.75)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(50 + 20)
                .with(UIProperty.MAX_X).equalTo(50 + 200 - 150)
                .with(UIProperty.WIDTH).equalTo(30);
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentWidthAndAnchorYSetProportionalToParentWidthCalculatesCorrectBounds() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorY(percentOf().parentWidth(0.25))
                                           .top(percentOf().parentWidth(0.1))
                                           .bottom(percentOf().parentWidth(0.05)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(50 + 20)
                .with(UIProperty.MAX_Y).equalTo(50 + 100 - 10)
                .with(UIProperty.HEIGHT).equalTo(70);
    }
}
