package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import fi.jakojaannos.roguelite.engine.ecs.annotation.DisableOn;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;

interface SystemInputRecord<T> {
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
                    "Entity data should not have manually defined constructors! [data=%s]",
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
        static <T> Resources<T> createFor(final Class<T> clazz) {
            LOG.debug("Parsing Resources {}", clazz.getName());
            validateIsRecord(clazz);

            final var recordComponents = resolveComponents(clazz);
            final var constructor = resolveConstructor(clazz, recordComponents);

            for (final RecordComponent recordComponent : recordComponents) {
                LOG.debug("-> {} {}",
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
            boolean[]excluded
    ) implements SystemInputRecord<T> {
        public static <T> EntityData<T> createFor(final Class<T> clazz) {
            LOG.debug("Parsing EntityData {}", clazz.getName());
            validateIsRecord(clazz);

            final var recordComponents = resolveComponents(clazz);
            final var constructor = resolveConstructor(clazz, recordComponents);

            final var componentTypes = Arrays.stream(recordComponents)
                                             .map(RecordComponent::getType)
                                             .toArray(Class<?>[]::new);

            final boolean[] excluded = new boolean[recordComponents.length];
            for (var i = 0; i < recordComponents.length; ++i) {
                excluded[i] = recordComponents[i].isAnnotationPresent(Without.class);
                LOG.debug("-> {} {} (excluded: {})",
                          recordComponents[i].getType().getSimpleName(),
                          recordComponents[i].getName(),
                          excluded[i]);
            }

            return new EntityData<>(constructor, componentTypes, excluded);
        }
    }

    record Events<T>(
            Constructor<T>constructor,
            Class<?>[]componentTypes,
            Class<?>[]enableOn,
            Class<?>[]disableOn
    ) implements SystemInputRecord<T> {
        public static <T> Events<T> createFor(final Class<T> clazz) {
            LOG.debug("Parsing EventData {}", clazz.getName());
            validateIsRecord(clazz);

            final var recordComponents = resolveComponents(clazz);
            final var constructor = resolveConstructor(clazz, recordComponents);

            final var componentTypes = Arrays.stream(recordComponents)
                                             .map(RecordComponent::getType)
                                             .toArray(Class<?>[]::new);

            final var enableOn = Arrays.stream(recordComponents)
                                       .filter(eventClass -> eventClass.isAnnotationPresent(EnableOn.class))
                                       .map(RecordComponent::getType)
                                       .toArray(Class<?>[]::new);
            final var disableOn = Arrays.stream(recordComponents)
                                        .filter(eventClass -> eventClass.isAnnotationPresent(DisableOn.class))
                                        .map(RecordComponent::getType)
                                        .toArray(Class<?>[]::new);

            LOG.debug("-> @EnableOn events: {}", Arrays.toString(enableOn));
            LOG.debug("-> @DisableOn events: {}", Arrays.toString(disableOn));

            return new Events<>(constructor, componentTypes, enableOn, disableOn);
        }
    }
}
