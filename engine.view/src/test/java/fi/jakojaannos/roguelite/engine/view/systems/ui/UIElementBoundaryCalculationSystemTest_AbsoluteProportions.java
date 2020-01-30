package fi.jakojaannos.roguelite.engine.view.systems.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.absolute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(0, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(0, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT, element.getProperty(UIProperty.MAX_Y).orElseThrow());
    }

    @Test
    void buildingUIElementWithPartialBoundsSetsOnlyDefinedPropertiesDefaultingOthersToFill() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .bottom(absolute(100)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(10, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(0, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - 100, element.getProperty(UIProperty.MAX_Y).orElseThrow());
    }

    @Test
    void buildingUIElementWithLeftAndRightCalculatesTheWidthAutomatically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .right(absolute(100)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_WIDTH - (10 + 100), element.getProperty(UIProperty.WIDTH).orElseThrow());
    }

    @Test
    void buildingUIElementWithTopAndBottomCalculatesTheHeightAutomatically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(42))
                                           .bottom(absolute(24)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_HEIGHT - (42 + 24), element.getProperty(UIProperty.HEIGHT).orElseThrow());
    }

    @Test
    void buildingUIElementWithLeftBoundAndWidthCalculatesHorizontalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(24))
                                           .width(absolute(42)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(24, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(66, element.getProperty(UIProperty.MAX_X).orElseThrow());
    }

    @Test
    void buildingUIElementWithRightBoundAndWidthCalculatesHorizontalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.right(absolute(24))
                                           .width(absolute(42)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));


        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_WIDTH - 66, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - 24, element.getProperty(UIProperty.MAX_X).orElseThrow());
    }

    @Test
    void buildingUIElementWithTopBoundAndHeightCalculatesVerticalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(24))
                                           .height(absolute(42)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(24, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(66, element.getProperty(UIProperty.MAX_Y).orElseThrow());
    }

    @Test
    void buildingUIElementWithBottomBoundAndHeightCalculatesVerticalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.bottom(absolute(24))
                                           .height(absolute(42)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_HEIGHT - 66, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - 24, element.getProperty(UIProperty.MAX_Y).orElseThrow());
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
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(100, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - 200, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - (100 + 200), element.getProperty(UIProperty.WIDTH).orElseThrow());
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
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(100, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - 200, element.getProperty(UIProperty.MAX_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - (100 + 200), element.getProperty(UIProperty.HEIGHT).orElseThrow());
    }

    @Test
    void buildingUIElementWithAnchorXOffsetsCorrectAmountHorizontally() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(absolute(100))
                                           .left(absolute(200)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(300, element.getProperty(UIProperty.MIN_X).orElseThrow());
    }

    @Test
    void buildingUIElementWithAnchorYOffsetsCorrectAmountVertically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorY(absolute(100))
                                           .top(absolute(200)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(300, element.getProperty(UIProperty.MIN_Y).orElseThrow());
    }
}
