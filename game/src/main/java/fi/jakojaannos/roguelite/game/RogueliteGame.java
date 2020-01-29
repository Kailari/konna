package fi.jakojaannos.roguelite.game;

import fi.jakojaannos.roguelite.engine.GameBase;
import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.UpdateableTimeManager;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.MainThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@RequiredArgsConstructor
public class RogueliteGame extends GameBase implements MainThread {
    public RogueliteGame(final UpdateableTimeManager timeManager) {
        super(timeManager);
    }

    @Override
    public GameState tick(
            final GameState state,
            final Events events
    ) {
        super.tick(state, events);

        // FIXME: This is ugly but works
        state.getWorld().createOrReplaceResource(MainThread.class, this);

        val inputs = state.getWorld().getOrCreateResource(Inputs.class);
        val mouse = state.getWorld().getOrCreateResource(Mouse.class);
        val inputEvents = events.getInput();

        while (inputEvents.hasEvents()) {
            val event = inputEvents.pollEvent();

            event.getAxis().ifPresent(input -> {
                if (input.getAxis() == InputAxis.Mouse.X_POS) {
                    mouse.position.x = input.getValue();
                } else if (input.getAxis() == InputAxis.Mouse.Y_POS) {
                    mouse.position.y = input.getValue();
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
                    mouse.clicked = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_SPACE) {
                    inputs.inputRestart = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_ESCAPE) {
                    inputs.inputMenu = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_LEFT_ALT) {
                    inputs.inputForceCloseA = input.getAction() != ButtonInput.Action.RELEASE;
                } else if (input.getButton() == InputButton.Keyboard.KEY_F4) {
                    inputs.inputForceCloseB = input.getAction() != ButtonInput.Action.RELEASE;
                }
            });
        }

        state.tick();

        if (inputs.inputForceCloseA && inputs.inputForceCloseB) {
            this.setFinished(true);
        }

        return selectNextState(state);
    }

    protected GameState selectNextState(final GameState state) {
        val stateManager = state.getWorld().getOrCreateResource(GameStateManager.class);
        if (stateManager.shouldShutDown()) {
            this.setFinished(true);
        }

        return stateManager.getNextState(state);
    }
}
