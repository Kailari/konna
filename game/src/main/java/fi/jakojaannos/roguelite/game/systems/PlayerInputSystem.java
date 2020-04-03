package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;

public class PlayerInputSystem implements ECSSystem {
    private final Vector2d tmpCursorPos = new Vector2d();

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

        final var inputs = world.getOrCreateResource(Inputs.class);
        final var mouse = world.getOrCreateResource(Mouse.class);
        final var cameraProperties = world.getOrCreateResource(CameraProperties.class);

        final var entityManager = world.getEntityManager();
        mouse.calculateCursorPositionRelativeToCamera(entityManager, cameraProperties, this.tmpCursorPos);

        final var inputHorizontal = (inputs.inputRight ? 1 : 0) - (inputs.inputLeft ? 1 : 0);
        final var inputVertical = (inputs.inputDown ? 1 : 0) - (inputs.inputUp ? 1 : 0);
        final boolean inputAttack = inputs.inputAttack;

        entities.forEach(entity -> {
            final var moveInput = world.getEntityManager().getComponentOf(entity, MovementInput.class).orElseThrow();
            final var attackInput = world.getEntityManager().getComponentOf(entity, WeaponInput.class).orElseThrow();
            final var attackAbility = world.getEntityManager().getComponentOf(entity, AttackAbility.class)
                                           .orElseThrow();
            moveInput.move.set(inputHorizontal,
                               inputVertical);
            attackInput.attack = inputAttack;
            attackAbility.targetPosition.set(this.tmpCursorPos);

            if (inputs.inputWeaponSlot0) {
                equipWeaponAtSlot(0, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot1) {
                equipWeaponAtSlot(1, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot2) {
                equipWeaponAtSlot(2, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot3) {
                equipWeaponAtSlot(3, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot4) {
                equipWeaponAtSlot(4, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot5) {
                equipWeaponAtSlot(5, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot6) {
                equipWeaponAtSlot(6, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot7) {
                equipWeaponAtSlot(7, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot8) {
                equipWeaponAtSlot(8, entityManager, entity, attackAbility);
            } else if (inputs.inputWeaponSlot9) {
                equipWeaponAtSlot(9, entityManager, entity, attackAbility);
            }

        });
    }

    private void equipWeaponAtSlot(
            final int slot,
            final EntityManager entityManager,
            final Entity entity,
            final AttackAbility attackAbility
    ) {
        attackAbility.equippedSlot = slot;
    }
}
