package fi.jakojaannos.roguelite.game.state;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.TurretArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.systems.*;
import fi.jakojaannos.roguelite.game.systems.characters.CharacterAttackSystem;
import fi.jakojaannos.roguelite.game.systems.characters.ai.AttackAIControllerSystem;
import fi.jakojaannos.roguelite.game.systems.characters.ai.FollowerAIControllerSystem;
import fi.jakojaannos.roguelite.game.systems.characters.movement.CharacterMovementSystem;
import fi.jakojaannos.roguelite.game.systems.characters.movement.JumpingCharacterMovementSystem;
import fi.jakojaannos.roguelite.game.systems.cleanup.CleanUpDeadEnemyKillsSystem;
import fi.jakojaannos.roguelite.game.systems.cleanup.CleanUpDeadPlayersSystem;
import fi.jakojaannos.roguelite.game.systems.cleanup.CleanUpEntitiesWithLifetime;
import fi.jakojaannos.roguelite.game.systems.cleanup.ReaperSystem;
import fi.jakojaannos.roguelite.game.systems.collision.ColliderDataCollectorSystem;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionEventCleanupSystem;
import fi.jakojaannos.roguelite.game.systems.collision.DestroyProjectilesOnCollisionSystem;
import fi.jakojaannos.roguelite.game.systems.collision.ProjectileToCharacterCollisionHandlerSystem;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyForceSystem;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyFrictionSystem;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;
import fi.jakojaannos.roguelite.game.weapons.Weapons;
import fi.jakojaannos.roguelite.game.world.WorldGenerator;

public class GameplayGameState extends GameState {
    public GameplayGameState(
            final long seed,
            final World world,
            final TimeManager timeManager
    ) {
        super(world);
        world.registerResource(new Weapons());
        world.registerResource(CameraProperties.class, new CameraProperties(world.createEntity(new Transform(),
                                                                                               new NoDrawTag())
                                                                                 .asLegacyEntity()));
        world.registerResource(new SessionStats(timeManager.getCurrentGameTime()));
        world.registerResource(new Mouse());
        world.registerResource(new Inputs());
        world.registerResource(new GameStateManager());

        final var player = PlayerArchetype.create(world, timeManager, new Transform(0, 0));
        player.addComponent(new CameraFollowTargetTag());

        final var players = new Players();
        players.setLocalPlayer(player.asLegacyEntity());
        world.registerResource(players);

        final var entityManager = world.getEntityManager();
        final var crosshair = entityManager.createEntity();
        entityManager.addComponentTo(crosshair, new Transform(-999.0, -999.0));
        entityManager.addComponentTo(crosshair, new CrosshairTag());
        final var crosshairCollider = new Collider(CollisionLayer.NONE);
        crosshairCollider.width = 0.3;
        crosshairCollider.height = 0.3;
        crosshairCollider.origin.set(0.15);
        entityManager.addComponentTo(crosshair, crosshairCollider);

        final var emptiness = new TileType(0, false);
        final var floor = new TileType(1, false);
        final var wall = new TileType(2, true);
        final var generator = new WorldGenerator<>(emptiness);
        generator.prepareInitialRoom(seed, world, floor, wall, 25, 45, 5, 5, 2);

        final var levelEntity = entityManager.createEntity();
        final var layer = new TileMapLayer(generator.getTileMap(), true);
        entityManager.addComponentTo(levelEntity, layer);

        TurretArchetype.create(entityManager, timeManager, new Transform(2.0, 0.0));

        entityManager.applyModifications();
    }

    @Override
    protected SystemDispatcher createDispatcher() {
        final var builder = SystemDispatcher.builder();
        final var input = builder.group("input")
                                 .withSystem(new FollowerAIControllerSystem())
                                 .withSystem(new AttackAIControllerSystem())
                                 .withSystem(new StalkerAIControllerSystem())
                                 .withSystem(new PlayerInputSystem())
                                 .withSystem(new RotatePlayerTowardsAttackTargetSystem())
                                 .buildGroup();

        final var earlyTick = builder.group("early-tick")
                                     .withSystem(new SpawnerSystem())
                                     .withSystem(new ColliderDataCollectorSystem())
                                     .withSystem(new ApplyFrictionSystem())
                                     .dependsOn(input)
                                     .buildGroup();

        final var characterTick = builder.group("character-tick")
                                         .withSystem(new CharacterAttackSystem())
                                         .withSystem(new CharacterMovementSystem())
                                         .withSystem(new JumpingCharacterMovementSystem())
                                         .dependsOn(input, earlyTick)
                                         .buildGroup();

        final var physicsTick = builder.group("physics-tick")
                                       .withSystem(new SnapToCursorSystem())
                                       .withSystem(new ApplyForceSystem())
                                       .withSystem(new ApplyVelocitySystem())
                                       .dependsOn(input, earlyTick, characterTick)
                                       .buildGroup();

        final var collisionHandler = builder.group("collision-handler")
                                            .withSystem(new ProjectileToCharacterCollisionHandlerSystem())
                                            .withSystem(new DestroyProjectilesOnCollisionSystem())
                                            .dependsOn(input, earlyTick, characterTick, physicsTick)
                                            .buildGroup();

        final var lateTick = builder.group("late-tick")
                                    .withSystem(new RotateTowardsVelocitySystem())
                                    .withSystem(new HealthUpdateSystem())
                                    .withSystem(new SplitOnDeathSystem())
                                    .withSystem(new CameraControlSystem())
                                    .dependsOn(input, earlyTick, characterTick, physicsTick, collisionHandler)
                                    .buildGroup();

        final var cleanup = builder.group("cleanup")
                                   .withSystem(new HandleEntitiesInAirSystem())
                                   .withSystem(new CollisionEventCleanupSystem())
                                   .withSystem(new RestartGameSystem())
                                   .withSystem(new UpdateSessionTimerSystem())
                                   .withSystem(new CleanUpDeadPlayersSystem())
                                   .withSystem(new CleanUpDeadEnemyKillsSystem())
                                   .withSystem(new CleanUpEntitiesWithLifetime())
                                   .withSystem(new ReaperSystem())
                                   .dependsOn(input, earlyTick, characterTick, physicsTick, collisionHandler, lateTick)
                                   .buildGroup();

        return builder.build();
    }
}
