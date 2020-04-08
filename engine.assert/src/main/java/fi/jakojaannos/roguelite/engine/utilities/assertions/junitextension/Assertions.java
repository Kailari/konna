package fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.assertions.AssertEquals;

public final class Assertions {
    private Assertions() {
    }

    public static void assertEqualsExt(final Vector2d expected, final Vector2d actual, final double delta) {
        AssertEquals.assertEquals(expected, actual, delta);
    }
}
