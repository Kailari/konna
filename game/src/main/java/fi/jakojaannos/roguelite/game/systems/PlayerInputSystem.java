package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;

public class PlayerInputSystem implements EcsSystem<PlayerInputSystem.Resources, PlayerInputSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var inputs = resources.inputs;
        final var cursorPosition = resources.cursorPosition();

        final var inputHorizontal = (inputs.inputRight ? 1 : 0) - (inputs.inputLeft ? 1 : 0);
        final var inputVertical = (inputs.inputUp ? 1 : 0) - (inputs.inputDown ? 1 : 0);
        final boolean inputAttack = inputs.inputAttack;

        entities.forEach(entity -> {
            final var movementInput = entity.getData().movementInput;
            final var weaponInput = entity.getData().weaponInput;
            final var attackAbility = entity.getData().attackAbility;

            movementInput.move.set(inputHorizontal, inputVertical);
            weaponInput.attack = inputAttack;
            attackAbility.targetPosition.set(cursorPosition);

            if (inputs.inputWeaponSlot0) {
                attackAbility.equippedSlot = 0;
            } else if (inputs.inputWeaponSlot1) {
                attackAbility.equippedSlot = 1;
            } else if (inputs.inputWeaponSlot2) {
                attackAbility.equippedSlot = 2;
            } else if (inputs.inputWeaponSlot3) {
                attackAbility.equippedSlot = 3;
            } else if (inputs.inputWeaponSlot4) {
                attackAbility.equippedSlot = 4;
            } else if (inputs.inputWeaponSlot5) {
                attackAbility.equippedSlot = 5;
            } else if (inputs.inputWeaponSlot6) {
                attackAbility.equippedSlot = 6;
            } else if (inputs.inputWeaponSlot7) {
                attackAbility.equippedSlot = 7;
            } else if (inputs.inputWeaponSlot8) {
                attackAbility.equippedSlot = 8;
            } else if (inputs.inputWeaponSlot9) {
                attackAbility.equippedSlot = 9;
            }

            weaponInput.reload = inputs.inputReload;
        });
    }

    public static record Resources(
            Inputs inputs,
            Mouse mouse,
            CameraProperties cameraProperties
    ) {
        public Vector2d cursorPosition() {
            return this.mouse.calculatePositionUnderCursor(this.cameraProperties);
        }
    }

    public static record EntityData(
            MovementInput movementInput,
            WeaponInput weaponInput,
            AttackAbility attackAbility,
            PlayerTag playerTag
    ) {}
}
