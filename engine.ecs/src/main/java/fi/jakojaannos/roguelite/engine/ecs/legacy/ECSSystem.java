package fi.jakojaannos.roguelite.engine.ecs.legacy;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;

@Deprecated
public interface ECSSystem {
    @Deprecated
    void declareRequirements(RequirementsBuilder requirements);

    @Deprecated
    void tick(Stream<Entity> entities, World world);
}
