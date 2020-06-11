package fi.jakojaannos.roguelite.engine.view.systems.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.EventBus;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.UIBuilder;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.absolute;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UIElementBoundaryResolverTest_AbsoluteProportions {
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
    void buildingUIElementWithoutDefiningBoundsDefaultsToFill() {
        UserInterface userInterface = uiBuilder
                .element("a", builder -> {})
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(0, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH, element.getBounds().getMaxX());
        assertEquals(0, element.getBounds().getMinY());
        assertEquals(VIEWPORT_HEIGHT, element.getBounds().getMaxY());
    }

    @Test
    void buildingUIElementWithPartialBoundsSetsOnlyDefinedPropertiesDefaultingOthersToFill() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.left(absolute(10))
                                           .bottom(absolute(100)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(10, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH, element.getBounds().getMaxX());
        assertEquals(0, element.getBounds().getMinY());
        assertEquals(VIEWPORT_HEIGHT - 100, element.getBounds().getMaxY());
    }

    @Test
    void buildingUIElementWithLeftAndRightCalculatesTheWidthAutomatically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.left(absolute(10))
                                           .right(absolute(100)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_WIDTH - (10 + 100), element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithTopAndBottomCalculatesTheHeightAutomatically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.top(absolute(42))
                                           .bottom(absolute(24)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_HEIGHT - (42 + 24), element.getBounds().getHeight());
    }

    @Test
    void buildingUIElementWithLeftBoundAndWidthCalculatesHorizontalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.left(absolute(24))
                                           .width(absolute(42)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(24, element.getBounds().getMinX());
        assertEquals(66, element.getBounds().getMaxX());
    }

    @Test
    void buildingUIElementWithRightBoundAndWidthCalculatesHorizontalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.right(absolute(24))
                                           .width(absolute(42)))
                .build();
        userInterface.update(new Mouse());


        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_WIDTH - 66, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH - 24, element.getBounds().getMaxX());
    }

    @Test
    void buildingUIElementWithTopBoundAndHeightCalculatesVerticalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.top(absolute(24))
                                           .height(absolute(42)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(24, element.getBounds().getMinY());
        assertEquals(66, element.getBounds().getMaxY());
    }

    @Test
    void buildingUIElementWithBottomBoundAndHeightCalculatesVerticalCoordinatesCorrectly() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.bottom(absolute(24))
                                           .height(absolute(42)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(VIEWPORT_HEIGHT - 66, element.getBounds().getMinY());
        assertEquals(VIEWPORT_HEIGHT - 24, element.getBounds().getMaxY());
    }

    @Test
    void buildingUIElementWithLeftRightAndWidthIgnoresTheWidth() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.left(absolute(100))
                                           .right(absolute(200))
                                           .width(absolute(20_000_000)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(100, element.getBounds().getMinX());
        assertEquals(VIEWPORT_WIDTH - 200, element.getBounds().getMaxX());
        assertEquals(VIEWPORT_WIDTH - (100 + 200), element.getBounds().getWidth());
    }

    @Test
    void buildingUIElementWithTopBottomAndHeightIgnoresTheHeight() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.top(absolute(100))
                                           .bottom(absolute(200))
                                           .height(absolute(20_000_000)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(100, element.getBounds().getMinY());
        assertEquals(VIEWPORT_HEIGHT - 200, element.getBounds().getMaxY());
        assertEquals(VIEWPORT_HEIGHT - (100 + 200), element.getBounds().getHeight());
    }

    @Test
    void buildingUIElementWithAnchorXOffsetsCorrectAmountHorizontally() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.anchorX(absolute(100))
                                           .left(absolute(200)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(300, element.getBounds().getMinX());
    }

    @Test
    void buildingUIElementWithAnchorYOffsetsCorrectAmountVertically() {
        UserInterface userInterface = uiBuilder
                .element("a",
                         builder -> builder.anchorY(absolute(100))
                                           .top(absolute(200)))
                .build();
        userInterface.update(new Mouse());

        final var element = userInterface.findElements(that -> that.hasName().equalTo("a"))
                                         .findFirst().orElseThrow();
        assertEquals(300, element.getBounds().getMinY());
    }
}
