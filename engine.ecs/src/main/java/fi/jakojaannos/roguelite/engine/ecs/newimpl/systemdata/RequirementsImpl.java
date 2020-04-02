package fi.jakojaannos.roguelite.engine.ecs.newimpl.systemdata;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.Requirements;

public class RequirementsImpl<TEntityData> implements Requirements<TEntityData> {
    private Constructor<TEntityData> constructor;
    private Class<?>[] componentTypes;

    @Override
    public Constructor<TEntityData> getEntityDataConstructor() {
        return this.constructor;
    }

    @Override
    public Class<?>[] getEntityDataComponentTypes() {
        return this.componentTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void entityData(final Class<TEntityData> dataClass) {
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

        final var selectedConstructor = (Constructor<TEntityData>) constructors[0];
        validateConstructor(dataClass, selectedConstructor, recordComponents);

        this.constructor = selectedConstructor;
        this.componentTypes = Arrays.stream(recordComponents)
                                    .map(RecordComponent::getType)
                                    .toArray(Class<?>[]::new);
    }

    private void validateIsRecord(final Class<TEntityData> dataClass) {
        if (!dataClass.isRecord()) {
            throw new IllegalRequirementsException(String.format(
                    "System entity data should be a record! [data=%s]",
                    dataClass.getSimpleName()));
        }
    }

    private static void validateHasConstructor(final Class<?> dataClass, final Constructor<?>[] constructors) {
        if (constructors.length == 0) {
            throw new IllegalRequirementsException(String.format(
                    "Could not find entity data constructor! [data=%s]",
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
                    "Entity data should not have duplicate component types! [data=%s]",
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
                        "Entity data constructor parameter type did not match record component! [data=%s, param=%s, " +
                                "record=%s]",
                        dataClass.getSimpleName(),
                        paramType.getSimpleName(),
                        recordType.getSimpleName()));
            }
        }
    }
}
