package fi.jakojaannos.roguelite.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.GameRunnerTimeManager;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.input.ButtonInput;
import fi.jakojaannos.riista.input.InputAxis;
import fi.jakojaannos.riista.input.InputButton;
import fi.jakojaannos.riista.input.InputEvent;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGameWithGameMode;
import static org.junit.jupiter.api.Assertions.*;

class RogueliteGameTest {
    @Test
    void inputsAreFalseByDefault() {
        whenGameWithGameMode(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)))
                .runsSingleTick()
                .expect(state -> {
                    final var inputs = state.world().fetchResource(Inputs.class);
                    assertFalse(inputs.inputLeft);
                    assertFalse(inputs.inputRight);
                    assertFalse(inputs.inputUp);
                    assertFalse(inputs.inputDown);
                    assertFalse(inputs.inputAttack);
                });
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
        whenGameWithGameMode(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)))
                .receivesInput(InputEvent.button(InputButton.Keyboard.valueOf(key),
                                                 ButtonInput.Action.PRESS))
                .runsSingleTick()
                .expect(state -> {
                    final var inputs = state.world().fetchResource(Inputs.class);
                    assertEquals(inputs.inputLeft, left);
                    assertEquals(inputs.inputRight, right);
                    assertEquals(inputs.inputUp, up);
                    assertEquals(inputs.inputDown, down);
                });
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
        final var axisPos = horizontal ? InputAxis.Mouse.X_POS : InputAxis.Mouse.Y_POS;

        whenGameWithGameMode(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)))
                .then(state -> {
                    final var mouse = state.world().fetchResource(Mouse.class);
                    if (horizontal) {
                        mouse.position.x = initial;
                    } else {
                        mouse.position.y = initial;
                    }
                })
                .receivesInput(InputEvent.axis(axisPos, newPos))
                .runsSingleTick()
                .expect(state -> {
                    final var mouse = state.world().fetchResource(Mouse.class);
                    assertEquals(newPos, horizontal ? mouse.position.x : mouse.position.y);
                });
    }

    @Test
    void mouseButtonEventsUpdateGameState() {
        whenGameWithGameMode(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)))
                .receivesInput(InputEvent.button(InputButton.Mouse.button(0),
                                                 ButtonInput.Action.PRESS))
                .runsSingleTick()
                .expect(state -> {
                    final var inputs = state.world().fetchResource(Inputs.class);
                    assertTrue(inputs.inputAttack);
                });
    }

    @Test
    void mouseButtonEventsDoNotUpdateGameStateIfButtonIsWrong() {
        whenGameWithGameMode(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)))
                .receivesInput(InputEvent.button(InputButton.Mouse.button(1),
                                                 ButtonInput.Action.PRESS))
                .receivesInput(InputEvent.button(InputButton.Mouse.button(2),
                                                 ButtonInput.Action.PRESS))
                .receivesInput(InputEvent.button(InputButton.Mouse.button(3),
                                                 ButtonInput.Action.PRESS))
                .receivesInput(InputEvent.button(InputButton.Mouse.button(4),
                                                 ButtonInput.Action.PRESS))
                .runsSingleTick()
                .expect(state -> {
                    final var inputs = state.world().fetchResource(Inputs.class);
                    assertFalse(inputs.inputAttack);
                });
    }

    @Test
    void releasingMouseButtonDisablesInput() {
        whenGameWithGameMode(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)))
                .receivesInput(InputEvent.button(InputButton.Mouse.button(0),
                                                 ButtonInput.Action.PRESS))
                .runsSingleTick()
                .receivesInput(InputEvent.button(InputButton.Mouse.button(0),
                                                 ButtonInput.Action.RELEASE))
                .runsSingleTick()
                .expect(state -> {
                    final var inputs = state.world().fetchResource(Inputs.class);
                    assertFalse(inputs.inputAttack);
                });
    }
}
