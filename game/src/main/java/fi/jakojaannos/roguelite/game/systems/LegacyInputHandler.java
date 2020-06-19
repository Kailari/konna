package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.resources.Events;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.input.ButtonInput;
import fi.jakojaannos.riista.input.InputAxis;
import fi.jakojaannos.riista.input.InputButton;
import fi.jakojaannos.riista.input.InputEvent;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;

/**
 * @deprecated Leftover legacy input handling. Used to be in <code>RogueliteGame.java</code>. New implementation should
 *         do something fancier, involving some way of handling key bindings, input contexts etc.
 */
@Deprecated
public class LegacyInputHandler implements EcsSystem<LegacyInputHandler.Resources, EcsSystem.NoEntities, LegacyInputHandler.EventData> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> noEntities,
            final EventData eventData
    ) {
        final var inputs = resources.inputs;
        final var mouse = resources.mouse;

        eventData.inputEvents.forEach(event -> {
            event.asAxis().ifPresent(input -> {
                if (input.axis() == InputAxis.Mouse.X_POS) {
                    mouse.position.x = input.value();
                } else if (input.axis() == InputAxis.Mouse.Y_POS) {
                    mouse.position.y = input.value();
                }
            });

            event.asButton().ifPresent(input -> {
                if (input.button() == InputButton.Keyboard.KEY_A) {
                    inputs.inputLeft = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_D) {
                    inputs.inputRight = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_W) {
                    inputs.inputUp = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_S) {
                    inputs.inputDown = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Mouse.button(0)) {
                    inputs.inputAttack = input.action() != ButtonInput.Action.RELEASE;
                    mouse.clicked = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_SPACE) {
                    inputs.inputRestart = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_ESCAPE) {
                    inputs.inputMenu = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_LEFT_ALT) {
                    inputs.inputForceCloseA = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_F4) {
                    inputs.inputForceCloseB = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_0) {
                    inputs.inputWeaponSlot0 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_1) {
                    inputs.inputWeaponSlot1 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_2) {
                    inputs.inputWeaponSlot2 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_3) {
                    inputs.inputWeaponSlot3 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_4) {
                    inputs.inputWeaponSlot4 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_5) {
                    inputs.inputWeaponSlot5 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_6) {
                    inputs.inputWeaponSlot6 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_7) {
                    inputs.inputWeaponSlot7 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_8) {
                    inputs.inputWeaponSlot8 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_9) {
                    inputs.inputWeaponSlot9 = input.action() != ButtonInput.Action.RELEASE;
                } else if (input.button() == InputButton.Keyboard.KEY_R) {
                    inputs.inputReload = input.action() != ButtonInput.Action.RELEASE;
                }
            });
        });
    }

    public static record EventData(Iterable<InputEvent>inputEvents) {}

    public static record Resources(
            Inputs inputs,
            Mouse mouse,
            Events events
    ) {
    }
}
