package fi.jakojaannos.roguelite.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@RequiredArgsConstructor
public class RogueliteGame extends GameBase {
    public RogueliteGame(final UpdateableTimeManager timeManager) {
        super(timeManager);
    }

    @Override
    public GameState tick(
            final GameState state,
            final Events events
    ) {
        final var inputs = state.getWorld().getOrCreateResource(Inputs.class);
        final var mouse = state.getWorld().getOrCreateResource(Mouse.class);
        final var inputEvents = events.getInput();

        // FIXME: Input handling should happen in some engine-level system and provide actual inputs
        //  via system events (once they are implemented) and/or through a resource.
        while (inputEvents.hasEvents()) {
            final var event = inputEvents.pollEvent();

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

        // NOTE: It is important that state is ticked first! state.tick(...) sets some engine -level
        // default provided resources which might be needed while flushing the task queue, which in
        // turn happens in the super.tick(...)
        state.tick(events, this);
        super.tick(state, events);

        if (inputs.inputForceCloseA && inputs.inputForceCloseB) {
            this.setFinished(true);
        }

        return selectNextState(state);
    }

    protected GameState selectNextState(final GameState state) {
        final var stateManager = state.getWorld().getOrCreateResource(GameStateManager.class);
        if (stateManager.shouldShutDown()) {
            this.setFinished(true);
        }

        return stateManager.getNextState(state);
    }
}
