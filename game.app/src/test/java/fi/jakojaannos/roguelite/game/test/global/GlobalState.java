package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.test.content.TestAssetManager;
import fi.jakojaannos.roguelite.game.test.view.TestRenderingBackend;
import fi.jakojaannos.roguelite.game.test.view.TestWindow;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;
import io.cucumber.java.Before;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    public static Events events;
    public static Queue<InputEvent> inputEvents;
    public static GameRenderer gameRenderer;
    public static TestWindow window;

    @Before
    public void before() {
        game = new Roguelite();
        Path assetRoot = Paths.get("../assets");
        gameRenderer = new RogueliteGameRenderer(assetRoot,
                                                 window = new TestWindow(800, 600),
                                                 new TestRenderingBackend(),
                                                 new TestAssetManager(assetRoot));
        events = new Events();
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
        inputEvents.forEach(events.getInput()::fire);
        game.tick(state, events);
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
