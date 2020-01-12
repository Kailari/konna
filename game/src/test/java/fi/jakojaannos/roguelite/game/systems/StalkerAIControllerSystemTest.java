package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StalkerAIControllerSystemTest {
    private StalkerAIControllerSystem system;
    private World world;
    private Transform playerPos, stalkerPos;
    private Entity stalker;
    private MovementStats stalkerStats;
    private StalkerAI stalkerAI;

    @BeforeEach
    void beforeEach() {
        system = new StalkerAIControllerSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);
        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);


        Entity player = entityManager.createEntity();
        this.playerPos = new Transform();
        entityManager.addComponentTo(player, playerPos);
        entityManager.addComponentTo(player, new PlayerTag());
        this.world.getOrCreateResource(Players.class).setLocalPlayer(player);


        stalker = entityManager.createEntity();
        this.stalkerPos = new Transform();
        entityManager.addComponentTo(stalker, stalkerPos);
        entityManager.addComponentTo(stalker, new CharacterInput());
        this.stalkerAI = new StalkerAI(
                100.0f,
                25.0f,
                20.0f);
        entityManager.addComponentTo(stalker, stalkerAI);
        this.stalkerStats = new MovementStats(
                1.0,
                100.0,
                800.0
        );
        entityManager.addComponentTo(stalker, stalkerStats);

        entityManager.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f,0.0f,50.0f,50.0f,1.7f",
                       "0.0f,0.0f,6.0f,6.0f,0.3f",
                       "0.0f,0.0f,1.0f,1.0f,0.3f"
               })
    void stalkerEnemySpeedDependingOnDistanceToPlayerIsSetCorrectly(
            double playerX,
            double playerY,
            double stalkerX,
            double stalkerY,
            double expectedSpeed
    ) {
        this.playerPos.setPosition(playerX, playerY);
        this.stalkerPos.setPosition(stalkerX, stalkerY);
        this.stalkerAI.jumpCoolDown = 100.0f;

        this.system.tick(Stream.of(stalker), this.world);

        assertEquals(expectedSpeed, stalkerStats.maxSpeed, 0.001f);
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f,0.0f,200.0f,200.0f,false",
                       "0.0f,0.0f,8.0f,8.0f,false",
                       "0.0f,0.0f,3.0f,3.0f,true",
                       "0.0f,0.0f,1.0f,1.0f,true"
               })
    void stalkerLeapAbilityIsUsedWhenNearPlayer(
            double playerX,
            double playerY,
            double stalkerX,
            double stalkerY,
            boolean expectedToUseAbility
    ) {
        this.playerPos.setPosition(playerX, playerY);
        this.stalkerPos.setPosition(stalkerX, stalkerY);
        this.stalkerAI.jumpCoolDown = 0.0f;

        this.system.tick(Stream.of(stalker), this.world);

        boolean didUseAbility = (stalkerAI.jumpCoolDown > 0);
        assertEquals(expectedToUseAbility, didUseAbility);
    }


    @Test
    void stalkerLeapAbilityCoolDownWorksCorrectly() {
        this.stalkerAI.jumpCoolDown = 0.0f;
        this.stalkerAI.jumpAbilityGoesCoolDownThisLong = 2.0f;

        this.system.tick(Stream.of(stalker), this.world);
        assertEquals(2.0f, this.stalkerAI.jumpCoolDown, 0.001f);

        for (int i = 0; i < 45; i++) {
            this.system.tick(Stream.of(stalker), this.world);
        }

        assertEquals(1.1f, stalkerAI.jumpCoolDown, 0.001f);

        // move player out so stalker doesn't instantly leap on them
        this.playerPos.setPosition(100.0f, 100.0f);

        for (int i = 0; i < 55; i++) {
            this.system.tick(Stream.of(stalker), this.world);
        }
        assertEquals(0.0f, stalkerAI.jumpCoolDown, 0.001f);
    }


}
