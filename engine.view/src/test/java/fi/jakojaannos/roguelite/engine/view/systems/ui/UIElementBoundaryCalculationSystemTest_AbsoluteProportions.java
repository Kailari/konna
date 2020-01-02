package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fi.jakojaannos.roguelite.engine.view.test.utilities.ui.AssertUI.assertUI;
import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.absolute;

public class UIElementBoundaryCalculationSystemTest_AbsoluteProportions {
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    private UIBuilder uiBuilder;

    @BeforeEach
    void beforeEach() {
        uiBuilder = UserInterface.builder(new Viewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
                                          (fontSize, text) -> fontSize / 1.5 * text.length());
    }

    @Test
    void buildingUIElementWithoutDefiningBoundsDefaultsToFill() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> { })
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(0)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH)
                .with(UIProperty.MIN_Y).equalTo(0)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT);
    }

    @Test
    void buildingUIElementWithPartialBoundsSetsOnlyDefinedPropertiesDefaultingOthersToFill() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .bottom(absolute(100)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(10)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH)
                .with(UIProperty.MIN_Y).equalTo(0)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT - 100);
    }

    @Test
    void buildingUIElementWithLeftAndRightCalculatesTheWidthAutomatically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .right(absolute(100)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.WIDTH).equalTo(VIEWPORT_WIDTH - (10 + 100));
    }

    @Test
    void buildingUIElementWithTopAndBottomCalculatesTheHeightAutomatically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(42))
                                           .bottom(absolute(24)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.HEIGHT).equalTo(VIEWPORT_HEIGHT - (42 + 24));
    }

    @Test
    void buildingUIElementWithLeftBoundAndWidthCalculatesHorizontalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(24))
                                           .width(absolute(42)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(24)
                .with(UIProperty.MAX_X).equalTo(66);
    }

    @Test
    void buildingUIElementWithRightBoundAndWidthCalculatesHorizontalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.right(absolute(24))
                                           .width(absolute(42)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(VIEWPORT_WIDTH - 66)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH - 24);
    }

    @Test
    void buildingUIElementWithTopBoundAndHeightCalculatesVerticalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(24))
                                           .height(absolute(42)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(24)
                .with(UIProperty.MAX_Y).equalTo(66);
    }

    @Test
    void buildingUIElementWithBottomBoundAndHeightCalculatesVerticalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.bottom(absolute(24))
                                           .height(absolute(42)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(VIEWPORT_HEIGHT - 66)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT - 24);
    }

    @Test
    void buildingUIElementWithLeftRightAndWidthIgnoresTheWidth() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(100))
                                           .right(absolute(200))
                                           .width(absolute(20_000_000)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(100)
                .with(UIProperty.MAX_X).equalTo(VIEWPORT_WIDTH - 200)
                .with(UIProperty.WIDTH).equalTo(VIEWPORT_WIDTH - (100 + 200));
    }

    @Test
    void buildingUIElementWithTopBottomAndHeightIgnoresTheHeight() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(100))
                                           .bottom(absolute(200))
                                           .height(absolute(20_000_000)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(100)
                .with(UIProperty.MAX_Y).equalTo(VIEWPORT_HEIGHT - 200)
                .with(UIProperty.HEIGHT).equalTo(VIEWPORT_HEIGHT - (100 + 200));
    }

    @Test
    void buildingUIElementWithAnchorXOffsetsCorrectAmountHorizontally() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(absolute(100))
                                           .left(absolute(200)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_X).equalTo(300);
    }

    @Test
    void buildingUIElementWithAnchorYOffsetsCorrectAmountVertically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorY(absolute(100))
                                           .top(absolute(200)))
                .build();
        userInterface.update(new Vector2d(0), false);

        assertUI(userInterface)
                .hasExactlyOneElementWithName("a")
                .with(UIProperty.MIN_Y).equalTo(300);
    }
}
