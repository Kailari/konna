package fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.assertions;

import org.joml.Vector2d;

import java.util.function.Supplier;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.assertions.AssertionUtils.*;
import static org.junit.jupiter.api.Assertions.fail;

public final class AssertEquals {
    private AssertEquals() {
    }

    public static void assertEquals(final Vector2d expected, final Vector2d actual, final double delta) {
        if (!vectorsAreEqual(expected, actual, delta)) {
            failNotEqual(expected, actual, (String) null);
        }
    }

    public static void assertEquals(
            final Vector2d expected,
            final Vector2d actual,
            final double delta,
            final String message
    ) {
        if (!vectorsAreEqual(expected, actual, delta)) {
            failNotEqual(expected, actual, message);
        }
    }

    public static void assertEquals(
            final Vector2d expected,
            final Vector2d actual,
            final double delta,
            final Supplier<String> messageSupplier
    ) {
        if (!vectorsAreEqual(expected, actual, delta)) {
            failNotEqual(expected, actual, messageSupplier);
        }
    }

    public static void assertNotEquals(final Vector2d expected, final Vector2d actual, final double delta) {
        if (vectorsAreEqual(expected, actual, delta)) {
            failEqual(actual, (String) null);
        }
    }

    public static void assertNotEquals(
            final Vector2d expected,
            final Vector2d actual,
            final double delta,
            final String message
    ) {
        if (vectorsAreEqual(expected, actual, delta)) {
            failEqual(actual, message);
        }
    }

    public static void assertNotEquals(
            final Vector2d expected,
            final Vector2d actual,
            final double delta,
            final Supplier<String> messageSupplier
    ) {
        if (vectorsAreEqual(expected, actual, delta)) {
            failEqual(actual, messageSupplier);
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void failNotEqual(
            final Vector2d expected,
            final Vector2d actual,
            final String message
    ) {
        AssertionUtils.fail(format(expected, actual, message), expected, actual);
    }

    private static void failNotEqual(
            final Vector2d expected,
            final Vector2d actual,
            final Supplier<String> messageSupplier
    ) {
        AssertionUtils.fail(format(expected, actual, nullSafeGet(messageSupplier)), expected, actual);
    }

    private static void failEqual(final Object actual, final Supplier<String> messageSupplier) {
        failEqual(actual, nullSafeGet(messageSupplier));
    }

    private static void failEqual(final Object actual, final String message) {
        fail(buildPrefix(message) + "expected: not equal but was: <" + actual + ">");
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private static boolean vectorsAreEqual(final Vector2d expected, final Vector2d actual, final double delta) {
        assertValidDelta(delta);
        return vectorsAreEqual(expected, actual) || expected.distance(actual) < delta;
    }

    private static boolean vectorsAreEqual(final Vector2d a, final Vector2d b) {
        return doublesAreEqual(a.x, b.x) && doublesAreEqual(a.y, b.y);
    }

    private static boolean doublesAreEqual(final double a, final double b) {
        return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void assertValidDelta(final double delta) {
        if (Double.isNaN(delta) || delta < 0.0) {
            failIllegalDelta(String.valueOf(delta));
        }
    }

    private static void failIllegalDelta(final String delta) {
        fail("positive delta expected but was: <" + delta + ">");
    }
}
