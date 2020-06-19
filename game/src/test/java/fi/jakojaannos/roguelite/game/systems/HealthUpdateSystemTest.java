package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthUpdateSystemTest {
    private EntityHandle entity;

    @ParameterizedTest
    @CsvSource({
                       "1.0f,1.0f,0.0f,false",
                       "100.0f,-1.0f,0.0f,true",
                       "25.0f,25.0f,25.0f,true",
                       "25.0f,25.0f,24.0f,false",
                       "25.0f,25.0f,400.0f,true",
                       "100.0f,-5.0f,5.0f,true",
                       "100.0f,25.0f,25.0f,true",
                       "100.0f,25.0f,5.0f,false"
               })
    void entitiesWithZeroHpAreAddedDeadTag(
            double maxHp,
            double currentHp,
            double damage,
            boolean shouldBeRemoved
    ) {
        whenGame().withSystems(new HealthUpdateSystem())
                  .withState(world -> {
                      world.registerResource(new SessionStats(0));

                      Health hp = new Health(maxHp, currentHp);
                      entity = world.createEntity(hp);
                      hp.addDamageInstance(new DamageInstance(damage, DamageSource.Generic.UNDEFINED), 0);
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      final var hasDeadTag = entity.hasComponent(DeadTag.class);
                      assertEquals(shouldBeRemoved, hasDeadTag);
                  });
    }
}
