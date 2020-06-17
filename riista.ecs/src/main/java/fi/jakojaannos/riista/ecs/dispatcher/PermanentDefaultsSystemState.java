package fi.jakojaannos.riista.ecs.dispatcher;

import java.util.Collection;

import fi.jakojaannos.riista.ecs.SystemGroup;
import fi.jakojaannos.riista.ecs.SystemState;
import fi.jakojaannos.riista.ecs.annotation.DisabledByDefault;

class PermanentDefaultsSystemState implements SystemState {
    @Override
    public boolean isEnabled(final Class<?> systemClass) {
        return !systemClass.isAnnotationPresent(DisabledByDefault.class);
    }

    @Override
    public boolean isEnabled(final SystemGroup systemGroup) {
        return systemGroup.isEnabledByDefault();
    }

    @Override
    public void setState(final Class<?> systemClass, final boolean state) {
    }

    @Override
    public void setState(final SystemGroup systemGroup, final boolean state) {
    }

    @Override
    public void resetToDefaultState(final Collection<Class<?>> system) {
    }

    @Override
    public void resetGroupsToDefaultState(final Collection<SystemGroup> systemGroups) {
    }
}
