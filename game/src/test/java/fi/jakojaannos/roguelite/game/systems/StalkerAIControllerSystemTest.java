package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StalkerAIControllerSystemTest {

    private EntityManager entityManager;
    private SimpleTimeManager timeManager;
    private StalkerAIControllerSystem system;
    private World world;
    private Transform playerPos, stalkerPos;
    private Entity stalker;
    private WalkingMovementAbility movementStats;
    private Physics physics;
    private StalkerAI stalkerAI;

    @BeforeEach
    void beforeEach() {
        system = new StalkerAIControllerSystem();
        entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);
        timeManager = new SimpleTimeManager(20);
        world.createOrReplaceResource(Time.class, new Time(timeManager));

        Entity player = entityManager.createEntity();
        this.playerPos = new Transform();
        entityManager.addComponentTo(player, playerPos);
        entityManager.addComponentTo(player, new PlayerTag());
        this.world.getOrCreateResource(Players.class).setLocalPlayer(player);

        stalker = entityManager.createEntity();
        this.stalkerAI = new StalkerAI();
        this.stalkerAI.moveSpeedWalk = 4.5;
        this.stalkerAI.moveSpeedSneak = 1.5;
        entityManager.addComponentTo(stalker, stalkerAI);
        entityManager.addComponentTo(stalker, new CharacterInput());
        this.stalkerPos = new Transform();
        entityManager.addComponentTo(stalker, stalkerPos);
        movementStats = new WalkingMovementAbility(1.0, 250.0);
        entityManager.addComponentTo(stalker, movementStats);
        entityManager.addComponentTo(stalker, physics = new Physics());
        physics.friction = 200.0;

        entityManager.applyModifications();
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
        this.playerPos.position.set(playerX, playerY);
        this.stalkerPos.position.set(stalkerX, stalkerY);
        this.stalkerAI.jumpCoolDownInTicks = 10000;
        this.stalkerAI.lastJumpTimeStamp = 10000;

        this.system.tick(Stream.of(stalker), this.world);

        assertEquals(expectedSpeed, movementStats.maxSpeed, 0.001f);
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
        this.playerPos.position.set(playerX, playerY);
        this.stalkerPos.position.set(stalkerX, stalkerY);
        this.stalkerAI.leapRadiusSquared = 50.0;
        this.stalkerAI.lastJumpTimeStamp = -100;
        this.stalkerAI.jumpCoolDownInTicks = 50;

        this.system.tick(Stream.of(stalker), this.world);
        entityManager.applyModifications();

        assertEquals(expectedToUseAbility, entityManager.hasComponent(stalker, InAir.class));
    }


    @Test
    void leapAbilityIsNotUsedWhileOnCooldown() {
        this.stalkerAI.jumpCoolDownInTicks = 20;
        this.stalkerAI.lastJumpTimeStamp = -100;

        this.system.tick(Stream.of(stalker), this.world);
        entityManager.applyModifications();
        assertTrue(entityManager.hasComponent(stalker, InAir.class));

        entityManager.removeComponentFrom(stalker, InAir.class);
        for (int i = 0; i < 10; i++) {
            timeManager.refresh();
        }

        this.system.tick(Stream.of(stalker), this.world);
        entityManager.applyModifications();
        assertFalse(entityManager.hasComponent(stalker, InAir.class));
    }
}
