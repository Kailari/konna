package fi.jakojaannos.roguelite.engine.utilities.math;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RotatedRectangleTest {
    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,0.0,0.0",
                       "0.5,0.25,0.0,0.0",
                       "100.0,200.25,0.0,0.0",
               })
    void getTopLeftReturnsCorrectValueForUnrotatedRectAtOriginWhenOriginIsNotSet(
            double width,
            double height,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(0.0),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getTopLeft(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,1.0,0.0",
                       "0.5,0.25,0.5,0.0",
                       "100.0,200.25,100.0,0.0",
               })
    void getTopRightReturnsCorrectValueForUnrotatedRectAtOriginWhenOriginIsNotSet(
            double width,
            double height,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(0.0),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getTopRight(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,0.0,1.0",
                       "0.5,0.25,0.0,0.25",
                       "100.0,200.25,0.0,200.25",
               })
    void getBottomLeftReturnsCorrectValueForUnrotatedRectAtOriginWhenOriginIsNotSet(
            double width,
            double height,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(0.0),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getBottomLeft(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,1.0,1.0",
                       "0.5,0.25,0.5,0.25",
                       "100.0,200.25,100.0,200.25",
               })
    void getBottomRightReturnsCorrectValueForUnrotatedRectAtOriginWhenOriginIsNotSet(
            double width,
            double height,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(0.0),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getBottomRight(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,16.0,25.0,16.0,25.0",
                       "0.5,0.25,32.2,123.4,32.2,123.4",
                       "100.0,200.25,43.4,-12.2,43.4,-12.2",
               })
    void getTopLeftReturnsCorrectValueForUnrotatedRectAtSomePositionWhenOriginIsNotSet(
            double width,
            double height,
            double positionX,
            double positionY,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(positionX, positionY),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getTopLeft(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,16.0,25.0,17.0,25.0",
                       "0.5,0.25,32.2,123.4,32.7,123.4",
                       "100.0,200.25,43.4,-12.2,143.4,-12.2",
               })
    void getTopRightReturnsCorrectValueForUnrotatedRectAtSomePositionWhenOriginIsNotSet(
            double width,
            double height,
            double positionX,
            double positionY,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(positionX, positionY),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getTopRight(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,16.0,25.0,16.0,26.0",
                       "0.5,0.25,32.2,123.4,32.2,123.65",
                       "100.0,200.25,43.4,-12.2,43.4,188.05",
               })
    void getBottomLeftReturnsCorrectValueForUnrotatedRectAtSomePositionWhenOriginIsNotSet(
            double width,
            double height,
            double positionX,
            double positionY,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(positionX, positionY),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getBottomLeft(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @ParameterizedTest
    @CsvSource({
                       "1.0,1.0,16.0,25.0,17.0,26.0",
                       "0.5,0.25,32.2,123.4,32.7,123.65",
                       "100.0,200.25,43.4,-12.2,143.4,188.05",
               })
    void getBottomRightReturnsCorrectValueForUnrotatedRectAtSomePositionWhenOriginIsNotSet(
            double width,
            double height,
            double positionX,
            double positionY,
            double expectedX,
            double expectedY
    ) {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(positionX, positionY),
                                                     new Vector2d(0.0),
                                                     width,
                                                     height,
                                                     0.0);

        Vector2d actual = rect.getBottomRight(new Vector2d());
        assertAll(
                () -> assertEquals(expectedX, actual.x),
                () -> assertEquals(expectedY, actual.y)
        );
    }

    @Test
    void getTopLeftReturnsCorrectValueWhenNotAtOriginAndRotatedButOriginIsNotSet() {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(-456, 123),
                                                     new Vector2d(0.0),
                                                     24.2,
                                                     32.3,
                                                     Math.toRadians(12.5));

        Vector2d actual = rect.getTopLeft(new Vector2d());
        assertAll(
                () -> assertEquals(-456, actual.x),
                () -> assertEquals(123, actual.y)
        );
    }

    @Test
    void getTopRightReturnsCorrectValueWhenNotAtOriginAndRotatedButOriginIsNotSet() {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(-456, 123),
                                                     new Vector2d(0.0),
                                                     24.2,
                                                     32.3,
                                                     Math.toRadians(12.5));

        Vector2d actual = rect.getTopRight(new Vector2d());
        assertAll(
                () -> assertEquals(-456 + 23.626363, actual.x, 0.02),
                () -> assertEquals(123 + 5.2378387, actual.y, 0.02)
        );
    }

    @Test
    void getBottomLeftReturnsCorrectValueWhenNotAtOriginAndRotatedButOriginIsNotSet() {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(-456, 123),
                                                     new Vector2d(0.0),
                                                     24.2,
                                                     32.3,
                                                     Math.toRadians(12.5));

        Vector2d actual = rect.getBottomLeft(new Vector2d());
        assertAll(
                () -> assertEquals(-456 - 6.9909995, actual.x, 0.02),
                () -> assertEquals(123 + 31.534361, actual.y, 0.02)
        );
    }

    @Test
    void getBottomRightReturnsCorrectValueWhenNotAtOriginAndRotatedButOriginIsNotSet() {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(-456, 123),
                                                     new Vector2d(0.0),
                                                     24.2,
                                                     32.3,
                                                     Math.toRadians(12.5));

        Vector2d actual = rect.getBottomRight(new Vector2d());
        assertAll(
                () -> assertEquals(-456 + 16.65364, actual.x, 0.02),
                () -> assertEquals(123 + 36.772200, actual.y, 0.02)
        );
    }

    @Test
    void getBottomRightReturnsCorrectValueWhenNotAtOriginAndRotatedAndOriginIsSet() {
        RotatedRectangle rect = new RotatedRectangle(new Vector2d(-456, 123),
                                                     new Vector2d(42, 69),
                                                     24.2,
                                                     32.3,
                                                     Math.toRadians(12.5));

        Vector2d actual = rect.getBottomRight(new Vector2d());
        // TODO: Actually verify these are correct with pen & paper
        assertAll(
                () -> assertEquals(-465.4347350, actual.x, 0.02),
                () -> assertEquals(83.317311, actual.y, 0.02)
        );
    }
}
