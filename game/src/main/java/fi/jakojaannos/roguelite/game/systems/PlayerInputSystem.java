package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.weapons.Weapon;

public class PlayerInputSystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerInputSystem.class);

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

            if (inputs.inputWeaponSlot0)
                tryEquipWeaponAtSlot(0, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot1)
                tryEquipWeaponAtSlot(1, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot2)
                tryEquipWeaponAtSlot(2, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot3)
                tryEquipWeaponAtSlot(3, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot4)
                tryEquipWeaponAtSlot(4, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot5)
                tryEquipWeaponAtSlot(5, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot6)
                tryEquipWeaponAtSlot(6, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot7)
                tryEquipWeaponAtSlot(7, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot8)
                tryEquipWeaponAtSlot(8, entityManager, entity, attackAbility);
            else if (inputs.inputWeaponSlot9)
                tryEquipWeaponAtSlot(9, entityManager, entity, attackAbility);

        });
    }

    private void tryEquipWeaponAtSlot(
            final int slot,
            final EntityManager entityManager,
            final Entity entity,
            final AttackAbility attackAbility
    ) {
        if (slot >= attackAbility.weaponList.length || slot < 0) {
            LOG.debug("Trying to equip a weapon that doesn't exist! index:" + slot);
            equipWeapon(attackAbility.unarmed, entityManager, entity, attackAbility);
        } else {
            LOG.debug("Equipping weapon at slot " + slot);
            equipWeapon(attackAbility.weaponList[slot], entityManager, entity, attackAbility);
        }
    }

    private void equipWeapon(
            final Weapon weapon,
            final EntityManager entityManager,
            final Entity entity,
            final AttackAbility attackAbility
    ) {
        attackAbility.equippedWeapon.unequip(entityManager, entity);
        attackAbility.equippedWeapon = weapon;
        attackAbility.equippedWeapon.equip(entityManager, entity);
    }
}
