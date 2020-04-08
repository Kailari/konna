package fi.jakojaannos.roguelite.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayDeque;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

import static org.junit.jupiter.api.Assertions.*;

class RogueliteGameTest {
    private GameRunner gameRunner;
    private GameState state;

    @BeforeEach
    void beforeEach() {
        gameRunner = new GameRunner() {
            @Override
            protected boolean shouldContinueLoop() {
                return true;
            }

            @Override
            protected void onStateChange(final GameState state) {
            }

            @Override
            protected void onModeChange(final GameMode gameMode) {
            }
        };
        state = gameRunner.setActiveGameMode(GameplayGameMode.create(6969));
    }

    @Test
    void inputsAreFalseByDefault() {
        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, ArrayDeque::new);

        Inputs inputs = state.world().fetchResource(Inputs.class);
        assertFalse(inputs.inputLeft);
        assertFalse(inputs.inputRight);
        assertFalse(inputs.inputUp);
        assertFalse(inputs.inputDown);
        assertFalse(inputs.inputAttack);
    }

    @ParameterizedTest
    @CsvSource({
                       "KEY_A,true,false,false,false",
                       "KEY_D,false,true,false,false",
                       "KEY_W,false,false,true,false",
                       "KEY_S,false,false,false,true",
               })
    void keyInputEventsUpdateStateAccordingly(
            String key,
            boolean left,
            boolean right,
            boolean up,
            boolean down
    ) {
        final var inputQueue = new ArrayDeque<InputEvent>();
        inputQueue.addLast(InputEvent.button(new ButtonInput(InputButton.Keyboard.valueOf(key), ButtonInput.Action.PRESS)));

        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, () -> inputQueue);

        Inputs inputs = state.world().fetchResource(Inputs.class);
        assertEquals(inputs.inputLeft, left);
        assertEquals(inputs.inputRight, right);
        assertEquals(inputs.inputUp, up);
        assertEquals(inputs.inputDown, down);
    }

    @ParameterizedTest
    @CsvSource({
                       "true,0.0,1.0", "false,0.0,1.0",
                       "true,0.0,0.0", "false,0.0,0.0",
                       "true,0.2,0.3", "false,0.2,0.3",
               })
    void mouseInputEventsUpdateGameState(
            boolean horizontal,
            double initial,
            double newPos
    ) {
        InputAxis.Mouse axisPos = horizontal ? InputAxis.Mouse.X_POS : InputAxis.Mouse.Y_POS;
        Mouse mouse = state.world().fetchResource(Mouse.class);
        if (horizontal) {
            mouse.position.x = initial;
        } else {
            mouse.position.y = initial;
        }

        final var inputQueue = new ArrayDeque<InputEvent>();
        inputQueue.addLast(InputEvent.axial(new AxialInput(axisPos, newPos)));

        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, () -> inputQueue);

        assertEquals(newPos, horizontal ? mouse.position.x : mouse.position.y);
    }

    @Test
    void mouseButtonEventsUpdateGameState() {
        final var inputQueue = new ArrayDeque<InputEvent>();
        inputQueue.addLast(ButtonInput.pressed(InputButton.Mouse.button(0)));

        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, () -> inputQueue);

        Inputs inputs = state.world().fetchResource(Inputs.class);
        assertTrue(inputs.inputAttack);
    }

    @Test
    void mouseButtonEventsDoNotUpdateGameStateIfButtonIsWrong() {
        final var inputQueue = new ArrayDeque<InputEvent>();
        inputQueue.addLast(ButtonInput.pressed(InputButton.Mouse.button(1)));
        inputQueue.addLast(ButtonInput.pressed(InputButton.Mouse.button(2)));
        inputQueue.addLast(ButtonInput.pressed(InputButton.Mouse.button(3)));
        inputQueue.addLast(ButtonInput.pressed(InputButton.Mouse.button(4)));

        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, () -> inputQueue);

        Inputs inputs = state.world().fetchResource(Inputs.class);
        assertFalse(inputs.inputAttack);
    }

    @Test
    void releasingMouseButtonDisablesInput() {
        final var inputQueue = new ArrayDeque<InputEvent>();

        // Pressed
        inputQueue.addLast(ButtonInput.pressed(InputButton.Mouse.button(0)));
        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, () -> inputQueue);
        inputQueue.clear();

        // Released
        inputQueue.addLast(ButtonInput.released(InputButton.Mouse.button(0)));
        accumulator.accumulate(20);
        gameRunner.simulateFrame(state, accumulator, () -> inputQueue);

        Inputs inputs = state.world().fetchResource(Inputs.class);
        assertFalse(inputs.inputAttack);
    }
}
