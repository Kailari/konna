package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

public record SystemInputRecord<T>(
        Constructor<T>constructor,
        Class<?>[]componentTypes
) {
    @SuppressWarnings("unchecked")
    public static <T> SystemInputRecord<T> createFor(final Class<T> dataClass) {
        validateIsRecord(dataClass);

        final var recordComponents = dataClass.getRecordComponents();
        validateNoDuplicateComponents(dataClass, recordComponents);

        final var constructors = dataClass.getConstructors();
        validateHasConstructor(dataClass, constructors);
        if (constructors.length > 1) {
            throw new IllegalRequirementsException(String.format(
                    "Entity data should not have manually defined constructors! [data=%s]",
                    dataClass.getSimpleName()));
        }

        final var selectedConstructor = (Constructor<T>) constructors[0];
        validateConstructor(dataClass, selectedConstructor, recordComponents);

        return new SystemInputRecord<>(selectedConstructor,
                                       Arrays.stream(recordComponents)
                                             .map(RecordComponent::getType)
                                             .toArray(Class<?>[]::new));
    }

    private static void validateIsRecord(final Class<?> dataClass) {
        if (!dataClass.isRecord()) {
            throw new IllegalRequirementsException(String.format(
                    "System inputs must be defined as records! [data=%s]",
                    dataClass.getSimpleName()));
        }
    }

    private static void validateHasConstructor(final Class<?> dataClass, final Constructor<?>[] constructors) {
        if (constructors.length == 0) {
            throw new IllegalRequirementsException(String.format(
                    "Could not find system input constructor! [data=%s]",
                    dataClass.getSimpleName()));
        }
    }

    private static void validateNoDuplicateComponents(
            final Class<?> dataClass,
            final RecordComponent[] recordComponents
    ) {
        final var distinctCount = Arrays.stream(recordComponents)
                                        .map(RecordComponent::getType)
                                        .distinct()
                                        .count();

        if (distinctCount < recordComponents.length) {
            throw new IllegalRequirementsException(String.format(
                    "System input should not have duplicate component types! "
                    + "[data=%s]",
                    dataClass.getSimpleName()));
        }
    }

    private static void validateConstructor(
            final Class<?> dataClass,
            final Constructor<?> constructor, final RecordComponent[] recordComponents
    ) {
        final var nParams = constructor.getParameterCount();
        final var parameterTypes = constructor.getParameterTypes();

        for (var i = 0; i < nParams; ++i) {
            final var paramType = parameterTypes[i];
            final var recordType = recordComponents[i].getType();

            final var isValidParam = paramType.isAssignableFrom(recordType);
            if (!isValidParam) {
                throw new IllegalRequirementsException(String.format(
                        "System input constructor parameter type did not match record component! "
                        + "[data=%s, param=%s, record=%s]",
                        dataClass.getSimpleName(),
                        paramType.getSimpleName(),
                        recordType.getSimpleName()));
            }
        }
    }
}
