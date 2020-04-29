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

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UIElementBoundaryResolverTest_ParentRelativeProportions {
    private static final int VIEWPORT_WIDTH = 200;
    private static final int VIEWPORT_HEIGHT = 100;

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
    void buildingUIElementWithLeftAndRightRelativeToParentWidthCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf(parentWidth(0.25)))
                                           .right(percentOf(parentWidth(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(50, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH - 20, element.getBounds().getMaxX());
        assertEquals(VIEWPORT_WIDTH - (50 + 20), element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToParentHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(percentOf(parentHeight(0.25)))
                                           .right(percentOf(parentHeight(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(25, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH - 10, element.getBounds().getMaxX());
        assertEquals(VIEWPORT_WIDTH - (25 + 10), element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentWidthCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(percentOf(parentWidth(0.25)))
                                           .bottom(percentOf(parentWidth(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(50, element.getBounds().getMinY());
        assertEquals(VIEWPORT_HEIGHT - 20, element.getBounds().getMaxY());
        assertEquals(VIEWPORT_HEIGHT - (50 + 20), element.getBounds().getHeight());
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentHeightCalculatesTheBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(percentOf(parentHeight(0.25)))
                                           .bottom(percentOf(parentHeight(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(25, element.getBounds().getMinY());
        assertEquals(VIEWPORT_HEIGHT - 10, element.getBounds().getMaxY());
        assertEquals(VIEWPORT_HEIGHT - (25 + 10), element.getBounds().getHeight());
    }

    @Test
    void buildingUIElementWithWidthRelativeToParentWidthCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .width(percentOf(parentWidth(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(10, element.getBounds().getMinX());
        assertEquals(10 + 20, element.getBounds().getMaxX());
        assertEquals(20, element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithWidthRelativeToParentHeightCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.left(absolute(10))
                                           .width(percentOf(parentHeight(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(10, element.getBounds().getMinX());
        assertEquals(10 + 10, element.getBounds().getMaxX());
        assertEquals(10, element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithHeightRelativeToParentWidthCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(10))
                                           .height(percentOf(parentWidth(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(10, element.getBounds().getMinY());
        assertEquals(10 + 20, element.getBounds().getMaxY());
        assertEquals(20, element.getBounds().getHeight());
    }

    @Test
    void buildingUIElementWithHeightRelativeToParentHeightCalculatesBoundsCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.top(absolute(10))
                                           .height(percentOf(parentHeight(0.1))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(10, element.getBounds().getMinY());
        assertEquals(10 + 10, element.getBounds().getMaxY());
        assertEquals(10, element.getBounds().getHeight());
    }

    @Test
    void buildingUIElementWithLeftAndRightRelativeToParentWidthAndAnchorXSetProportionalToParentWidthCalculatesCorrectBounds() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(percentOf(parentWidth(0.25)))
                                           .left(percentOf(parentWidth(0.1)))
                                           .right(percentOf(parentWidth(0.75))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(50 + 20, element.getBounds().getMinX());
        assertEquals(50 + 200 - 150, element.getBounds().getMaxX());
        assertEquals(30, element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithTopAndBottomRelativeToParentWidthAndAnchorYSetProportionalToParentWidthCalculatesCorrectBounds() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         UIElementType.PANEL,
                         builder -> builder.anchorY(percentOf(parentWidth(0.25)))
                                           .top(percentOf(parentWidth(0.1)))
                                           .bottom(percentOf(parentWidth(0.05))))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(50 + 20, element.getBounds().getMinY());
        assertEquals(50 + 100 - 10, element.getBounds().getMaxY());
        assertEquals(70, element.getBounds().getHeight());
    }
}
