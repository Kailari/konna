package fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension;

import org.joml.Vector2d;

import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.assertions.AssertEquals;

public final class Assertions {
    private Assertions() {
    }

    public static void assertEqualsExt(
            final Vector2d expected,
            final Vector2d actual,
            final double delta
    ) {
        AssertEquals.assertEquals(expected, actual, delta);
    }

    public static void assertEqualsExt(
            final Vector2d expected,
            final Vector2d actual,
            final double delta,
            final String message
    ) {
        AssertEquals.assertEquals(expected, actual, delta, message);
    }

    public static void assertEqualsExt(
            final Vector2d expected,
            final Vector2d actual,
            final double delta,
            final Supplier<String> messageSupplier
    ) {
        AssertEquals.assertEquals(expected, actual, delta, messageSupplier);
    }

    public static void assertNotEqualsExt(
            final Vector2d unexpected,
            final Vector2d actual,
            final double delta
    ) {
        AssertEquals.assertNotEquals(unexpected, actual, delta);
    }

    public static void assertNotEqualsExt(
            final Vector2d unexpected,
            final Vector2d actual,
            final double delta,
            final String message
    ) {
        AssertEquals.assertNotEquals(unexpected, actual, delta, message);
    }

    public static void assertNotEqualsExt(
            final Vector2d unexpected,
            final Vector2d actual,
            final double delta,
            final Supplier<String> messageSupplier
    ) {
        AssertEquals.assertNotEquals(unexpected, actual, delta, messageSupplier);
    }
}
