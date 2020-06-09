package fi.jakojaannos.roguelite.engine.utilities;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class OptionalUtil {
    private OptionalUtil() {
    }

    /**
     * Returns the value wrapped in optional if all given condition optionals are present.
     *
     * @param value      value to wrap
     * @param conditions List of optionals that need to be present
     * @param <T>        Type of the value to return
     *
     * @return <code>Optional.empty()</code> if any of the conditions are empty, otherwise
     * <code>value</code> wrapped in an optional
     */
    public static <T> Optional<T> ifAllPresent(
            final T value,
            final Optional<?>... conditions
    ) {
        if (Arrays.stream(conditions).allMatch(Optional::isPresent)) {
            return Optional.of(value);
        }

        return Optional.empty();
    }

    /**
     * Returns the value if all given condition optionals are present.
     *
     * @param value      value to return
     * @param conditions List of optionals that need to be present
     * @param <T>        Type of the value to return
     *
     * @return <code>Optional.empty()</code> if any of the conditions are empty, otherwise
     * <code>value</code>
     */
    public static <T> Optional<T> ifAllPresentOptional(
            final Optional<T> value,
            final Optional<?>... conditions
    ) {
        if (Arrays.stream(conditions).allMatch(Optional::isPresent)) {
            return value;
        }

        return Optional.empty();
    }

    /**
     * Returns the value wrapped in optional if none of the given condition optionals are present.
     *
     * @param value      value to wrap
     * @param conditions List of optionals that need to be empty
     * @param <T>        Type of the value to return
     *
     * @return <code>Optional.empty()</code> if any of the conditions are present, otherwise
     * <code>value</code> wrapped in an optional
     */
    public static <T> Optional<T> ifNonePresent(
            final T value,
            final Optional<?>... conditions
    ) {
        if (Arrays.stream(conditions).allMatch(Optional::isEmpty)) {
            return Optional.of(value);
        }

        return Optional.empty();
    }

    /**
     * Returns the value if all given condition optionals are present.
     *
     * @param value      value to return
     * @param conditions List of optionals that need to be empty
     * @param <T>        Type of the value to return
     *
     * @return <code>Optional.empty()</code> if any of the conditions are present, otherwise
     * <code>value</code>
     */
    public static <T> Optional<T> ifNonePresentOptional(
            final Optional<T> value,
            final Optional<?>... conditions
    ) {
        if (Arrays.stream(conditions).allMatch(Optional::isEmpty)) {
            return value;
        }

        return Optional.empty();
    }

    /**
     * Returns the value wrapped in optional if any of the given condition optionals are empty.
     *
     * @param value      value to wrap
     * @param conditions List of optionals of which one needs to be empty
     * @param <T>        Type of the value to return
     *
     * @return <code>Optional.empty()</code> if none of the conditions are empty, otherwise
     * <code>value</code> wrapped in an optional
     */
    public static <T> Optional<T> ifAnyEmpty(
            final T value,
            final Optional<?>... conditions
    ) {
        for (final var condition : conditions) {
            if (condition.isEmpty()) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the value if any of the given condition optionals are empty.
     *
     * @param value      value to return
     * @param conditions List of optionals of which one needs to be empty
     * @param <T>        Type of the value to return
     *
     * @return <code>Optional.empty()</code> if none of the conditions are empty, otherwise
     * <code>value</code> wrapped in an optional
     */
    public static <T> Optional<T> ifAnyEmptyOptional(
            final Optional<T> value,
            final Optional<?>... conditions
    ) {
        for (final var condition : conditions) {
            if (condition.isEmpty()) {
                return value;
            }
        }

        return Optional.empty();
    }

    /**
     * Applies the given binary operator on the given arguments, if they are both present.
     *
     * @param a        Left-hand side argument
     * @param b        Right-hand side argument
     * @param operator Binary operator to apply
     * @param <T1>     Type of the LHS argument
     * @param <T2>     Type of the RHS argument
     * @param <R>      Type of the result
     *
     * @return <code>Optional.empty()</code> if either of the arguments is not present. Output from
     * the <code>operator</code> applied on the arguments otherwise.
     */
    public static <T1, T2, R> Optional<R> applyIfBothArePresent(
            final Optional<T1> a,
            final Optional<T2> b,
            final BiFunction<T1, T2, R> operator
    ) {
        return (a.isPresent() && b.isPresent())
                ? Optional.ofNullable(operator.apply(a.get(), b.get()))
                : Optional.empty();
    }
}
