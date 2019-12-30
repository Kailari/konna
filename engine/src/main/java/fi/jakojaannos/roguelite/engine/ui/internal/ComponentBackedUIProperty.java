package fi.jakojaannos.roguelite.engine.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ui.UIProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class ComponentBackedUIProperty<T, C extends Component> implements UIProperty<T> {
    @Getter private final String name;
    private final Class<? extends C> componentClass;
    private final Function<C, T> componentToValueMapper;

    public Optional<T> getValueFromEntity(
            final Entity entity,
            final EntityManager entityManager
    ) {
        return entityManager.getComponentOf(entity, this.componentClass)
                            .map(this.componentToValueMapper);
    }
}
