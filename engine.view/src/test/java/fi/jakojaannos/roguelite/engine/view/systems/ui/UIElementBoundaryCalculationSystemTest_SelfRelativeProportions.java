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
import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.percentOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class UIElementBoundaryCalculationSystemTest_SelfRelativeProportions {
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    private UIBuilder uiBuilder;

    @BeforeEach
    void beforeEach() {
        uiBuilder = UserInterface.builder(new Viewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
                                          (fontSize, text) -> fontSize / 1.5 * text.length());
    }

    @Test
    void buildingUIElementWithWidthRelativeToSelfThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownWidth(0.1)))
                .build()
                .update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithHeightRelativeToSelfThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(percentOf().ownHeight(0.1)))
                .build()
                .update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithWidthAndHeightRelativeToEachOtherThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .height(percentOf().ownWidth(0.1)))
                .build()
                .update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithWidthRelativeToHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .height(absolute(100)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithWidthRelativeToDefaultHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
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

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithHeightRelativeToWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(100))
                                           .height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithHeightRelativeToDefaultWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
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

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnWidthThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().ownWidth(0.1))
                                           .right(percentOf().ownWidth(0.05)))
                .build()
                .update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithLeftRelativeToOwnWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(200))
                                           .left(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithRightRelativeToOwnWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(200))
                                           .right(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithTopRelativeToOwnHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .top(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithBottomRelativeToOwnHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .bottom(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnHeightWithoutSettingHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().ownHeight(0.1))
                                           .right(percentOf().ownHeight(0.05)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(mock(Time.class), new Mouse(), mock(Events.class)));
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
        userInterface.update(mock(Time.class), new Mouse(), mock(Events.class));

        final var element = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("a"))
                                         .findFirst().orElseThrow();
        assertEquals(50, element.getProperty(UIProperty.MIN_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - 20, element.getProperty(UIProperty.MAX_X).orElseThrow());
        assertEquals(VIEWPORT_WIDTH - (50 + 20), element.getProperty(UIProperty.WIDTH).orElseThrow());
    }
}
