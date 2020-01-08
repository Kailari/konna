package fi.jakojaannos.roguelite.game.state;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.BasicTurretArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.systems.*;
import fi.jakojaannos.roguelite.game.systems.cleanup.CleanUpDeadEnemyKillsSystem;
import fi.jakojaannos.roguelite.game.systems.cleanup.CleanUpDeadPlayersSystem;
import fi.jakojaannos.roguelite.game.systems.cleanup.ReaperSystem;
import fi.jakojaannos.roguelite.game.systems.collision.*;
import fi.jakojaannos.roguelite.game.world.WorldGenerator;
import lombok.val;

import java.util.Arrays;

public class GameplayGameState extends GameState {
    public GameplayGameState(
            final long seed,
            final World world,
            final TimeManager timeManager
    ) {
        super(world, timeManager);

        val entityManager = world.getEntityManager();

        val player = PlayerArchetype.create(entityManager,
                                            new Transform(0, 0));
        world.getOrCreateResource(Players.class).setLocalPlayer(player);
        entityManager.addComponentTo(player, new CameraFollowTargetTag());

        val camera = entityManager.createEntity();
        entityManager.addComponentTo(camera, new Transform());
        entityManager.addComponentTo(camera, new NoDrawTag());
        world.getOrCreateResource(CameraProperties.class).cameraEntity = camera;

        val crosshair = entityManager.createEntity();
        entityManager.addComponentTo(crosshair, new Transform(-999.0, -999.0));
        entityManager.addComponentTo(crosshair, new CrosshairTag());
        val crosshairCollider = new Collider(CollisionLayer.NONE);
        crosshairCollider.width = 0.3;
        crosshairCollider.height = 0.3;
        crosshairCollider.origin.set(0.15);
        entityManager.addComponentTo(crosshair, crosshairCollider);

        val emptiness = new TileType(0, false);
        val floor = new TileType(1, false);
        val wall = new TileType(2, true);
        val generator = new WorldGenerator<TileType>(emptiness);
        generator.prepareInitialRoom(seed, world, floor, wall, 25, 45, 5, 5, 2);

        val levelEntity = entityManager.createEntity();
        val layer = new TileMapLayer(generator.getTileMap());
        layer.collisionEnabled = true;
        entityManager.addComponentTo(levelEntity, layer);

        val sessionStats = world.getOrCreateResource(SessionStats.class);
        sessionStats.endTimeStamp = sessionStats.beginTimeStamp = timeManager.getCurrentGameTime();
        BasicTurretArchetype.create(entityManager, new Transform(2.0,0.0));

        entityManager.applyModifications();
    }

    @Override
    protected SystemDispatcher createDispatcher() {
        return SystemDispatcher
                .builder()
                .withGroups(SystemGroups.values())
                .addGroupDependencies(SystemGroups.CLEANUP, Arrays.stream(SystemGroups.values())
                                                                  .filter(group -> group != SystemGroups.CLEANUP)
                                                                  .toArray(SystemGroup[]::new))
                .addGroupDependencies(SystemGroups.EARLY_TICK, SystemGroups.INPUT)
                .addGroupDependencies(SystemGroups.CHARACTER_TICK, SystemGroups.INPUT, SystemGroups.EARLY_TICK)
                .addGroupDependencies(SystemGroups.PHYSICS_TICK, SystemGroups.CHARACTER_TICK, SystemGroups.EARLY_TICK)
                .addGroupDependencies(SystemGroups.COLLISION_HANDLER, SystemGroups.PHYSICS_TICK)
                .addGroupDependencies(SystemGroups.LATE_TICK, SystemGroups.COLLISION_HANDLER, SystemGroups.PHYSICS_TICK, SystemGroups.CHARACTER_TICK)
                .withSystem(new ColliderDataCollectorSystem())
                .withSystem(new PlayerInputSystem())
                .withSystem(new CharacterMovementSystem())
                .withSystem(new CharacterAttackSystem())
                .withSystem(new ApplyVelocitySystem())
                .withSystem(new SnapToCursorSystem())
                .withSystem(new CharacterAIControllerSystem())
                .withSystem(new StalkerAIControllerSystem())
                .withSystem(new SlimeAIControllerSystem())
                .withSystem(new SlimeDeathHandlerSystem())
                .withSystem(new CameraControlSystem())
                .withSystem(new SpawnerSystem())
                .withSystem(new ProjectileToCharacterCollisionHandlerSystem())
                .withSystem(new DestroyProjectilesOnCollisionSystem())
                .withSystem(new CollisionEventCleanupSystem())
                .withSystem(new HealthUpdateSystem())
                .withSystem(new EnemyAttackCoolDownSystem())
                .withSystem(new EnemyToPlayerCollisionHandlerSystem())
                .withSystem(new ReaperSystem())
                .withSystem(new RotatePlayerTowardsAttackTargetSystem())
                .withSystem(new RestartGameSystem())
                .withSystem(new UpdateSessionTimerSystem())
                .withSystem(new CleanUpDeadPlayersSystem())
                .withSystem(new CleanUpDeadEnemyKillsSystem())
                .withSystem(new BasicTurretControllerSystem())
                .build();
    }
}
