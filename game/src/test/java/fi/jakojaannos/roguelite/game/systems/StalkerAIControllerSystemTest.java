package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.expectEntity;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StalkerAIControllerSystemTest {
    private Transform playerTransform;
    private Transform stalkerTransform;
    private EntityHandle stalker;
    private WalkingMovementAbility movementStats;
    private StalkerAI stalkerAI;

    void beforeEach(final World world) {
        playerTransform = new Transform();
        final var player = world.createEntity(playerTransform, new PlayerTag());

        final var players = new Players(player);
        world.registerResource(players);

        stalkerAI = new StalkerAI();
        stalkerAI.moveSpeedWalk = 4.5;
        stalkerAI.moveSpeedSneak = 1.5;
        movementStats = new WalkingMovementAbility(1.0, 250.0);
        stalkerTransform = new Transform();
        stalker = world.createEntity(stalkerAI,
                                     stalkerTransform,
                                     movementStats,
                                     new MovementInput(),
                                     Physics.builder()
                                            .friction(200)
                                            .build());
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f, 0.0f, 50.0f, 50.0f, 4.5f",
                       "0.0f, 0.0f, 6.0f, 6.0f, 1.5f",
                       "0.0f, 0.0f, 1.0f, 1.0f, 4.5f"
               })
    void stalkerSpeedDependsOnDistanceToPlayer(
            double playerX,
            double playerY,
            double stalkerX,
            double stalkerY,
            double expectedSpeed
    ) {
        whenGame().withSystems(new StalkerAIControllerSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.playerTransform.position.set(playerX, playerY);
                      this.stalkerTransform.position.set(stalkerX, stalkerY);
                      this.stalkerAI.jumpCoolDownInTicks = 10000;
                      this.stalkerAI.lastJumpTimeStamp = 10000;
                  })
                  .runsSingleTick()
                  .expect(state -> assertEquals(expectedSpeed, movementStats.maxSpeed, 0.001f));
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f, 0.0f, 200.0f, 200.0f, false",
                       "0.0f, 0.0f, 8.0f, 8.0f, false",
                       "0.0f, 0.0f, 3.0f, 3.0f, true",
                       "0.0f, 0.0f, 1.0f, 1.0f, true"
               })
    void stalkerLeapAbilityIsUsedWhenNearPlayer(
            double playerX,
            double playerY,
            double stalkerX,
            double stalkerY,
            boolean expectedToUseAbility
    ) {
        whenGame().withSystems(new StalkerAIControllerSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.playerTransform.position.set(playerX, playerY);
                      this.stalkerTransform.position.set(stalkerX, stalkerY);
                      this.stalkerAI.leapRadiusSquared = 50.0;
                      this.stalkerAI.lastJumpTimeStamp = -100;
                      this.stalkerAI.jumpCoolDownInTicks = 50;
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      if (expectedToUseAbility) {
                          expectEntity(stalker).toHaveComponent(InAir.class);
                      } else {
                          expectEntity(stalker).toNotHaveComponent(InAir.class);
                      }
                  });
    }

    @Test
    void leapAbilityIsNotUsedWhileOnCooldown() {
        whenGame().withSystems(new StalkerAIControllerSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.stalkerAI.jumpCoolDownInTicks = 20;

                      final var timeManager = world.fetchResource(TimeManager.class);
                      this.stalkerAI.lastJumpTimeStamp = timeManager.getCurrentGameTime();
                  })
                  .runsSingleTick()
                  .expect(state -> expectEntity(stalker).toNotHaveComponent(InAir.class));
    }

    @Test
    void leapAbilityIsUsedAfterOnCooldown() {
        whenGame().withSystems(new StalkerAIControllerSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      this.stalkerAI.jumpCoolDownInTicks = 5;

                      final var timeManager = world.fetchResource(TimeManager.class);
                      this.stalkerAI.lastJumpTimeStamp = timeManager.getCurrentGameTime();
                  })
                  .runsForTicks(7)
                  .expect(state -> expectEntity(stalker).toHaveComponent(InAir.class));
    }
}
