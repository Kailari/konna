package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.GameState;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class SpawnerSystem implements ECSSystem {
    private static final Collection<Class<? extends Component>> REQUIRED_COMPONENTS = List.of(
            SpawnerComponent.class, Transform.class
    );

    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return REQUIRED_COMPONENTS;
    }

    private final Vector2d temp = new Vector2d();

    @Override
    public void tick(
            Stream<Entity> entities,
            World world,
            double delta
    ) {
        Entities cluster = world.getEntities();

        entities.forEach(entity -> {

            val myPos = cluster.getComponentOf(entity, Transform.class).get();
            val spawnComp = cluster.getComponentOf(entity, SpawnerComponent.class).get();

            spawnComp.spawnCoolDown -= delta;

            if (spawnComp.spawnCoolDown <= 0.0f) {
                spawnComp.spawnCoolDown = spawnComp.spawnFrequency;

                Entity spawned = spawnComp.entityFactory.apply(cluster);
                cluster.applyModifications();

                cluster.getComponentOf(spawned, Transform.class).ifPresent(spawnedPos -> {
                    myPos.getCenter(temp);

                    spawnedPos.setPosition(
                            temp.x - spawnedPos.getWidth() / 2,
                            temp.y - spawnedPos.getHeight() / 2);


                });
            }
        });

    }
}
