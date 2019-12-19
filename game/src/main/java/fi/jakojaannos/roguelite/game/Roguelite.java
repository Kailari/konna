package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.game.data.resources.GameStatus;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.systems.*;
import fi.jakojaannos.roguelite.game.systems.collision.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.Queue;

@Slf4j
public class Roguelite extends GameBase {
    private final SystemDispatcher dispatcher;

    public Roguelite() {
        this.dispatcher = SystemDispatcher
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
                .withSystem(new CleanUpDeadPlayersSystem())
                .withSystem(new RotatePlayerTowardsAttackTargetSystem())
                .withSystem(new RestartGameSystem())
                .build();
    }

    public GameState createInitialState() {
        return createInitialState(System.nanoTime());
    }

    public GameState createInitialState(long seed) {
        val entities = EntityManager.createNew(256, 32);
        return new GameplayGameState(seed, World.createNew(entities), getTime());
    }

    @Override
    public GameState tick(
            final GameState state,
            final Queue<InputEvent> inputEvents
    ) {
        val inputs = state.getWorld().getOrCreateResource(Inputs.class);
        val mouse = state.getWorld().getOrCreateResource(Mouse.class);

        while (!inputEvents.isEmpty()) {
            val event = inputEvents.remove();

            event.getAxis().ifPresent(input -> {
                if (input.getAxis() == InputAxis.Mouse.X_POS) {
                    mouse.pos.x = input.getValue();
                } else if (input.getAxis() == InputAxis.Mouse.Y_POS) {
                    mouse.pos.y = input.getValue();
                }
            });

            event.getButton().ifPresent(input -> {
                if (input.getButton() == InputButton.Keyboard.KEY_A) {
                    inputs.inputLeft = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_D) {
                    inputs.inputRight = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_W) {
                    inputs.inputUp = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_S) {
                    inputs.inputDown = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Mouse.button(0)) {
                    inputs.inputAttack = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_SPACE) {
                    inputs.inputRestart = input.getAction() != ButtonInput.Action.RELEASE;
                }
            });
        }

        this.dispatcher.dispatch(state.getWorld());
        state.getWorld().getEntityManager().applyModifications();

        if (state.getWorld().getOrCreateResource(GameStatus.class).shouldRestart) {
            return this.createInitialState();
        }

        return state;
    }
}
