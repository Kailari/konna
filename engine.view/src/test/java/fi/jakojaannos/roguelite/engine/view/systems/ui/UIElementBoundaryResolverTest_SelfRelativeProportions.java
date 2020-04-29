package fi.jakojaannos.roguelite.engine.view.systems.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.EventBus;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.absolute;
import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.percentOf;
import static org.junit.jupiter.api.Assertions.*;

public class UIElementBoundaryResolverTest_SelfRelativeProportions {
    private static final int VIEWPORT_WIDTH = 800;
    private static final int VIEWPORT_HEIGHT = 600;

    private UIBuilder uiBuilder;

    @BeforeEach
    void beforeEach() {
        final var events = new Events(new EventBus<>(), new EventBus<>(), new EventBus<>(), new EventBus<>());
        final var timeManager = new SimpleTimeManager(20);
        uiBuilder = UserInterface.builder(events,
                                          timeManager,
                                          new Viewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT),
                                          (fontSize, text) -> fontSize / 1.5 * text.length());
    }

    @Test
    void buildingUIElementWithWidthRelativeToSelfThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownWidth(0.1)))
                .build()
                .update(new Mouse()));
    }

    @Test
    void buildingUIElementWithHeightRelativeToSelfThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(percentOf().ownHeight(0.1)))
                .build()
                .update(new Mouse()));
    }

    @Test
    void buildingUIElementWithWidthAndHeightRelativeToEachOtherThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .height(percentOf().ownWidth(0.1)))
                .build()
                .update(new Mouse()));
    }

    @Test
    void buildingUIElementWithWidthRelativeToHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1))
                                           .height(absolute(100)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithWidthRelativeToDefaultHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(percentOf().ownHeight(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
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

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithHeightRelativeToWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(100))
                                           .height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithHeightRelativeToDefaultWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(percentOf().ownWidth(0.1)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
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

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnWidthThrows() {
        assertThrows(IllegalStateException.class, () -> uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().ownWidth(0.1))
                                           .right(percentOf().ownWidth(0.05)))
                .build()
                .update(new Mouse()));
    }

    @Test
    void buildingUIElementWithLeftRelativeToOwnWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(200))
                                           .left(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithRightRelativeToOwnWidthDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.width(absolute(200))
                                           .right(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithTopRelativeToOwnHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .top(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithBottomRelativeToOwnHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.height(absolute(200))
                                           .bottom(percentOf().ownWidth(0.5)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToOwnHeightWithoutSettingHeightDoesNotThrow() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf().ownHeight(0.1))
                                           .right(percentOf().ownHeight(0.05)))
                .build();

        assertDoesNotThrow(() -> userInterface.update(new Mouse()));
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
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(50, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH - 20, element.getBounds().getMaxX());
        assertEquals(VIEWPORT_WIDTH - (50 + 20), element.getBounds().getWidth());
    }
}
