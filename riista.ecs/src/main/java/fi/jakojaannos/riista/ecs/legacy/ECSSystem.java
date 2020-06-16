package fi.jakojaannos.riista.ecs.legacy;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.World;

@Deprecated
public interface ECSSystem {
    @Deprecated
    void declareRequirements(RequirementsBuilder requirements);

    @Deprecated
    void tick(Stream<Entity> entities, World world);
}
