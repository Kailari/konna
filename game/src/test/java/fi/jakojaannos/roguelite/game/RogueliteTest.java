package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class RogueliteTest {
    @Test
    void inputsAreFalseByDefault() {
        Roguelite roguelite = new Roguelite();
        GameState state = roguelite.createInitialState();
        roguelite.tick(state, new ArrayDeque<>());

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
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
        Roguelite roguelite = new Roguelite();
        GameState state = roguelite.createInitialState();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(new InputEvent(new ButtonInput(InputButton.Keyboard.valueOf(key), ButtonInput.Action.PRESS)));

        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
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
        Roguelite roguelite = new Roguelite();
        GameState state = roguelite.createInitialState();
        Mouse mouse = state.getWorld().getOrCreateResource(Mouse.class);
        if (horizontal) {
            mouse.pos.x = initial;
        } else {
            mouse.pos.y = initial;
        }

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(new InputEvent(new AxialInput(axisPos, newPos)));

        roguelite.tick(state, events);

        assertEquals(newPos, horizontal ? mouse.pos.x : mouse.pos.y);
    }

    @Test
    void mouseButtonEventsUpdateGameState() {
        Roguelite roguelite = new Roguelite();
        GameState state = roguelite.createInitialState();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(0)));

        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
        assertTrue(inputs.inputAttack);
    }

    @Test
    void mouseButtonEventsDoNotUpdateGameStateIfButtonIsWrong() {
        Roguelite roguelite = new Roguelite();
        GameState state = roguelite.createInitialState();

        Queue<InputEvent> events = new ArrayDeque<>();
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(1)));
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(2)));
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(3)));
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(4)));

        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
        assertFalse(inputs.inputAttack);
    }

    @Test
    void releasingMouseButtonDisablesInput() {
        Roguelite roguelite = new Roguelite();
        GameState state = roguelite.createInitialState();
        Queue<InputEvent> events = new ArrayDeque<>();

        // Pressed
        events.offer(ButtonInput.pressed(InputButton.Mouse.button(0)));
        roguelite.tick(state, events);

        // Released
        events.offer(ButtonInput.released(InputButton.Mouse.button(0)));
        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
        assertFalse(inputs.inputAttack);
    }
}
