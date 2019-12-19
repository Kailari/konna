package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.game.Roguelite;
import io.cucumber.java.Before;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

/**
 * Singleton game state to be used by step definitions. Note that this effectively prevents the E2E
 * tests from running from parallel in single test runner instance.
 */
public class GlobalState {
    public static Roguelite game;
    public static GameState state;
    public static Queue<InputEvent> inputEvents;

    @Before
    public void before() {
        game = new Roguelite();
        inputEvents = new ArrayDeque<>();
    }

    public static <T extends Component> Optional<T> getComponentOf(
            Entity player,
            Class<T> componentClass
    ) {
        return state.getWorld()
                    .getEntityManager()
                    .getComponentOf(player, componentClass);
    }

    public static void simulateTick() {
        game.tick(state, inputEvents);
        game.updateTime();
        inputEvents.clear();
    }

    public static void simulateSeconds(double seconds) {
        int ticks = (int) (seconds / 0.02);
        for (int i = 0; i < ticks; ++i) {
            simulateTick();
        }
    }
}
