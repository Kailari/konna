package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.riista.ecs.legacy.ECSSystem;
import fi.jakojaannos.riista.ecs.legacy.Entity;
import fi.jakojaannos.riista.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;

public class PlayerInputSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(MovementInput.class)
                    .withComponent(WeaponInput.class)
                    .withComponent(AttackAbility.class)
                    .withComponent(PlayerTag.class)
                    .requireResource(Inputs.class)
                    .requireResource(Mouse.class)
                    .requireResource(CameraProperties.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var inputs = world.fetchResource(Inputs.class);
        final var mouse = world.fetchResource(Mouse.class);
        final var cameraProperties = world.fetchResource(CameraProperties.class);

        final var cursorPosition = mouse.calculatePositionUnderCursor(cameraProperties);

        final var inputHorizontal = (inputs.inputRight ? 1 : 0) - (inputs.inputLeft ? 1 : 0);
        final var inputVertical = (inputs.inputUp ? 1 : 0) - (inputs.inputDown ? 1 : 0);
        final boolean inputAttack = inputs.inputAttack;

        entities.forEach(entity -> {
            final var moveInput = world.getEntityManager().getComponentOf(entity, MovementInput.class).orElseThrow();
            final var attackInput = world.getEntityManager().getComponentOf(entity, WeaponInput.class).orElseThrow();
            final var attackAbility = world.getEntityManager().getComponentOf(entity, AttackAbility.class)
                                           .orElseThrow();
            moveInput.move.set(inputHorizontal, inputVertical);
            attackInput.attack = inputAttack;
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

            attackInput.reload = inputs.inputReload;
        });
    }
}
