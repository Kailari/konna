package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.LogCategories;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisableOn;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;

public interface SystemInputRecord<T> {
    Logger LOG = LoggerFactory.getLogger(SystemInputRecord.class);

    Constructor<T> constructor();

    Class<?>[] componentTypes();

    private static RecordComponent[] resolveComponents(final Class<?> clazz) {
        final var recordComponents = clazz.getRecordComponents();
        validateNoDuplicateComponents(clazz, recordComponents);

        return recordComponents;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> resolveConstructor(
            final Class<T> clazz,
            final RecordComponent[] recordComponents
    ) {
        final var constructors = clazz.getConstructors();
        validateHasConstructor(clazz, constructors);
        if (constructors.length > 1) {
            throw new IllegalRequirementsException(String.format(
                    "System input records should not have manually defined constructors! [data=%s]",
                    clazz.getSimpleName()));
        }

        final var selectedConstructor = (Constructor<T>) constructors[0];
        validateConstructor(clazz, selectedConstructor, recordComponents);

        return selectedConstructor;
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

    record Resources<T>(
            Constructor<T>constructor,
            Class<?>[]componentTypes
    ) implements SystemInputRecord<T> {
        public static <T> Resources<T> createFor(final Class<T> clazz) {
            LOG.debug(LogCategories.SYSTEM_DATA_DUMP, "Parsing Resources {}", clazz.getName());
            validateIsRecord(clazz);

            final var recordComponents = resolveComponents(clazz);
            final var constructor = resolveConstructor(clazz, recordComponents);

            for (final RecordComponent recordComponent : recordComponents) {
                LOG.debug(LogCategories.SYSTEM_DATA_DUMP, "-> {} {}",
                          recordComponent.getType().getSimpleName(),
                          recordComponent.getName());
            }

            final var componentTypes = Arrays.stream(recordComponents)
                                             .map(RecordComponent::getType)
                                             .toArray(Class<?>[]::new);

            return new Resources<>(constructor, componentTypes);
        }
    }

    record EntityData<T>(
            Constructor<T>constructor,
            Class<?>[]componentTypes,
            boolean[]excluded,
            boolean[]optional
    ) implements SystemInputRecord<T> {
        public static <T> EntityData<T> createFor(final Class<T> clazz) {
            LOG.debug(LogCategories.SYSTEM_DATA_DUMP,
                      "Parsing EntityData {}", clazz.getName());
            validateIsRecord(clazz);

            final var recordComponents = resolveComponents(clazz);
            final var constructor = resolveConstructor(clazz, recordComponents);

            final var componentTypes = new Class<?>[recordComponents.length];
            final var excluded = new boolean[recordComponents.length];
            final var optional = new boolean[recordComponents.length];
            for (var i = 0; i < recordComponents.length; ++i) {
                resolveActualType(recordComponents[i], componentTypes, optional, excluded, i);

                LOG.debug(LogCategories.SYSTEM_DATA_DUMP, "-> {} {} (excluded: {}, optional: {})",
                          componentTypes[i].getSimpleName(),
                          recordComponents[i].getName(),
                          excluded[i],
                          optional[i]);

                if (excluded[i] && optional[i]) {
                    throw new IllegalRequirementsException("Illegal EntityData definition: Optional component \""
                                                           + recordComponents[i].getName()
                                                           + "\" marked as excluded! This does not make sense. Remove "
                                                           + "either the `Optional` or the `@Without`");
                }
            }

            return new EntityData<>(constructor, componentTypes, excluded, optional);
        }

        public static void resolveActualType(
                final RecordComponent recordComponent,
                final Class<?>[] componentTypes,
                final boolean[] optional,
                final boolean[] excluded,
                final int index
        ) {
            // Exclude if marked as excluded
            excluded[index] = recordComponent.isAnnotationPresent(Without.class);

            final var componentClass = recordComponent.getType();
            if (Optional.class.isAssignableFrom(componentClass)) {
                final var genericType = recordComponent.getGenericType();
                if (genericType instanceof ParameterizedType paramType) {
                    // It is optional and parameterized, we may safely assume it also has a single type parameter
                    final var typeParam = paramType.getActualTypeArguments()[0];

                    // The returned type parameter is actually always instance of `Class`, even though the signature
                    // claims it may be any `Type`. Do checked cast, just to be safe. This is executed only once, so
                    // we are not in a hurry here.
                    if (typeParam instanceof Class<?> paramClass) {
                        // All checks passed, it actually is an `Optional<T>`
                        // Use the unwrapped `T` for component type and mark as optional
                        componentTypes[index] = paramClass;
                        optional[index] = true;
                        return;
                    }
                }
            }

            // Not optional or some other check failed. Use the type directly and mark as non-optional.
            componentTypes[index] = componentClass;
            optional[index] = false;
        }
    }

    record Events<T>(
            Constructor<T>constructor,
            Class<?>[]componentTypes,
            boolean[]enableOn,
            boolean[]disableOn
    ) implements SystemInputRecord<T> {
        public static <T> Events<T> createFor(final Class<T> clazz) {
            LOG.debug(LogCategories.SYSTEM_DATA_DUMP,
                      "Parsing EventData {}", clazz.getName());
            validateIsRecord(clazz);

            final var recordComponents = resolveComponents(clazz);
            final var constructor = resolveConstructor(clazz, recordComponents);

            final var componentTypes = Arrays.stream(recordComponents)
                                             .map(RecordComponent::getType)
                                             .toArray(Class<?>[]::new);

            final boolean[] enableOn = new boolean[recordComponents.length];
            final boolean[] disableOn = new boolean[recordComponents.length];
            for (var i = 0; i < recordComponents.length; ++i) {
                enableOn[i] = recordComponents[i].isAnnotationPresent(EnableOn.class);
                disableOn[i] = recordComponents[i].isAnnotationPresent(DisableOn.class);
                LOG.debug(LogCategories.SYSTEM_DATA_DUMP, "-> {} {} (@EnableOn: {}, @DisableOn: {})",
                          recordComponents[i].getType().getSimpleName(),
                          recordComponents[i].getName(),
                          enableOn[i], disableOn[i]);
            }

            return new Events<>(constructor, componentTypes, enableOn, disableOn);
        }
    }
}
