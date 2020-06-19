package fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.assertions;

import org.opentest4j.AssertionFailedError;

import java.util.function.Supplier;

public class AssertionUtils {
    static void fail(final String message, final Object expected, final Object actual) {
        throw new AssertionFailedError(message, expected, actual);
    }

    static String format(final Object expected, final Object actual, final String message) {
        return buildPrefix(message) + formatValues(expected, actual);
    }

    static String buildPrefix(final String message) {
        return (nullSafeIsBlank(message) ? "" : message + " ==> ");
    }

    static String formatValues(final Object expected, final Object actual) {
        final var expectedString = toString(expected);
        final var actualString = toString(actual);
        if (expectedString.equals(actualString)) {
            return String.format("expected: %s but was: %s", formatClassAndValue(expected, expectedString),
                                 formatClassAndValue(actual, actualString));
        }
        return String.format("expected: <%s> but was: <%s>", expectedString, actualString);
    }

    private static String formatClassAndValue(final Object value, final String valueString) {
        final var classAndHash = getClassName(value) + toHash(value);
        return (value instanceof Class ? "<" + classAndHash + ">" : classAndHash + "<" + valueString + ">");
    }

    private static String toString(final Object obj) {
        if (obj instanceof Class) {
            return getCanonicalName((Class<?>) obj);
        }
        return obj == null ? null : obj.toString();
    }

    static String getCanonicalName(final Class<?> clazz) {
        try {
            final var canonicalName = clazz.getCanonicalName();
            return (canonicalName != null ? canonicalName : clazz.getName());
        } catch (final Throwable t) {
            return clazz.getName();
        }
    }

    static String nullSafeGet(final Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }

    static boolean nullSafeIsBlank(final String message) {
        return message == null || message.isBlank();
    }

    private static String toHash(final Object obj) {
        return (obj == null ? "" : "@" + Integer.toHexString(System.identityHashCode(obj)));
    }

    private static String getClassName(final Object obj) {
        return (obj == null ? "null"
                : obj instanceof Class ? getCanonicalName((Class<?>) obj) : obj.getClass().getName());
    }
}
