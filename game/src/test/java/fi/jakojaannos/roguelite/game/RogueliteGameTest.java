package fi.jakojaannos.roguelite.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;

import static org.junit.jupiter.api.Assertions.*;

class RogueliteGameTest {
    private RogueliteGame roguelite;
    private GameState state;

    @BeforeEach
    void beforeEach() {
        roguelite = new RogueliteGame();
        state = new GameplayGameState(6969,
                                      World.createNew(),
                                      roguelite.getTime());
    }

    @Test
    void inputsAreFalseByDefault() {
        roguelite.tick(state, new Events());

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
        Events events = new Events();
        events.input()
              .fire(InputEvent.button(new ButtonInput(InputButton.Keyboard.valueOf(key), ButtonInput.Action.PRESS)));

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
        Mouse mouse = state.getWorld().getOrCreateResource(Mouse.class);
        if (horizontal) {
            mouse.position.x = initial;
        } else {
            mouse.position.y = initial;
        }

        Events events = new Events();
        events.input().fire(InputEvent.axial(new AxialInput(axisPos, newPos)));

        roguelite.tick(state, events);

        assertEquals(newPos, horizontal ? mouse.position.x : mouse.position.y);
    }

    @Test
    void mouseButtonEventsUpdateGameState() {
        Events events = new Events();
        events.input().fire(ButtonInput.pressed(InputButton.Mouse.button(0)));

        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
        assertTrue(inputs.inputAttack);
    }

    @Test
    void mouseButtonEventsDoNotUpdateGameStateIfButtonIsWrong() {
        Events events = new Events();
        events.input().fire(ButtonInput.pressed(InputButton.Mouse.button(1)));
        events.input().fire(ButtonInput.pressed(InputButton.Mouse.button(2)));
        events.input().fire(ButtonInput.pressed(InputButton.Mouse.button(3)));
        events.input().fire(ButtonInput.pressed(InputButton.Mouse.button(4)));

        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
        assertFalse(inputs.inputAttack);
    }

    @Test
    void releasingMouseButtonDisablesInput() {
        Events events = new Events();

        // Pressed
        events.input().fire(ButtonInput.pressed(InputButton.Mouse.button(0)));
        roguelite.tick(state, events);

        // Released
        events.input().fire(ButtonInput.released(InputButton.Mouse.button(0)));
        roguelite.tick(state, events);

        Inputs inputs = state.getWorld().getOrCreateResource(Inputs.class);
        assertFalse(inputs.inputAttack);
    }
}
