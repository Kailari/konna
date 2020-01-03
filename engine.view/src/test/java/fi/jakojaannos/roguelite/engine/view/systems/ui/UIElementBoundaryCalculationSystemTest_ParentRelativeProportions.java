package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.absolute;
import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.percentOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class UIElementBoundaryCalculationSystemTest_ParentRelativeProportions {
    private static final int VIEWPORT_WIDTH = 200;
    private static final int VIEWPORT_HEIGHT = 100;

    private UIBuilder uiBuilder;

    @BeforeEach
    void beforeEach() {
        uiBuilder = UserInterface.builder(new Viewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
                                          (fontSize, text) -> fontSize / 1.5 * text.length());
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToParentWidthCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().parentWidth(0.25))
                                           .right(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(50, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - 20, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - (50 + 20), element.getProperty(UIProperty.WIDTH).orElseThrow());
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToParentHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().parentHeight(0.25))
                                           .right(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(25, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - 10, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - (25 + 10), element.getProperty(UIProperty.WIDTH).orElseThrow());
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentWidthCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(percentOf().parentWidth(0.25))
                                           .bottom(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(50, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - 20, element.getProperty(UIProperty.MAX_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - (50 + 20), element.getProperty(UIProperty.HEIGHT).orElseThrow());
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(percentOf().parentHeight(0.25))
                                           .bottom(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(25, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - 10, element.getProperty(UIProperty.MAX_Y).orElseThrow());
        assertEquals(VIEWPORT_HEIGHT - (25 + 10), element.getProperty(UIProperty.HEIGHT).orElseThrow());
    }

    @Test
    void buildingUIElementWithWidthRelativeToParentWidthCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .width(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(10, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(10 + 20, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(20, element.getProperty(UIProperty.WIDTH).orElseThrow());
    }

    @Test
    void buildingUIElementWithWidthRelativeToParentHeightCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .width(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(10, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(10 + 10, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(10, element.getProperty(UIProperty.WIDTH).orElseThrow());
    }

    @Test
    void buildingUIElementWithHeightRelativeToParentWidthCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(10))
                                           .height(percentOf().parentWidth(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(10, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(10 + 20, element.getProperty(UIProperty.MAX_Y).orElseThrow());
        assertEquals(20, element.getProperty(UIProperty.HEIGHT).orElseThrow());
    }

    @Test
    void buildingUIElementWithHeightRelativeToParentHeightCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(10))
                                           .height(percentOf().parentHeight(0.1)))
                .build();
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(10, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(10 + 10, element.getProperty(UIProperty.MAX_Y).orElseThrow());
        assertEquals(10, element.getProperty(UIProperty.HEIGHT).orElseThrow());
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
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(50 + 20, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(50 + 200 - 150, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(30, element.getProperty(UIProperty.WIDTH).orElseThrow());
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
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        val element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                   .findFirst().orElseThrow();
        assertEquals(50 + 20, element.getProperty(UIProperty.MIN_Y).orElseThrow());
        assertEquals(50 + 100 - 10, element.getProperty(UIProperty.MAX_Y).orElseThrow());
        assertEquals(70, element.getProperty(UIProperty.HEIGHT).orElseThrow());
    }
}
