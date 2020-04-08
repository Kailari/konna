package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.weapons.Weapons;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInputSystemTest {
    private PlayerInputSystem system;
    private World world;
    private MovementInput movementInput;
    private WeaponInput weaponInput;
    private Entity player;
    private AttackAbility abilities;

    @BeforeEach
    void beforeEach() {
        system = new PlayerInputSystem();
        this.world = World.createNew();
        world.registerResource(Inputs.class, new Inputs());
        EntityManager entityManager = world.getEntityManager();
        world.registerResource(Weapons.class, new Weapons());

        player = entityManager.createEntity();
        this.abilities = new AttackAbility(new DamageSource.LegacyEntity(player),
                                           CollisionLayer.PLAYER,
                                           0.0,
                                           0.0);
        entityManager.addComponentTo(player, movementInput = new MovementInput());
        entityManager.addComponentTo(player, weaponInput = new WeaponInput());
        entityManager.addComponentTo(player, this.abilities);
        entityManager.addComponentTo(player, new PlayerTag());

        entityManager.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f,0.0f,false,false,false,false",
                       "-1.0f,0.0f,true,false,false,false",
                       "1.0f,0.0f,false,true,false,false",
                       "0.0f,-1.0f,false,false,true,false",
                       "0.0f,1.0f,false,false,false,true",
                       "0.0f,0.0f,false,false,true,true",
                       "0.0f,0.0f,true,true,false,false",
                       "0.0f,0.0f,true,true,true,true",
               })
    void havingInputFlagsSetModifiesCharacterInputCorrectly(
            float expectedHorizontal,
            float expectedVertical,
            boolean left,
            boolean right,
            boolean up,
            boolean down
    ) {
        world.registerResource(new CameraProperties(null));
        Inputs inputs = this.world.fetchResource(Inputs.class);
        inputs.inputLeft = left;
        inputs.inputRight = right;
        inputs.inputUp = up;
        inputs.inputDown = down;
        system.tick(Stream.of(player), this.world);
        world.getEntityManager().applyModifications();

        assertEquals(expectedHorizontal, this.movementInput.move.x);
        assertEquals(expectedVertical, this.movementInput.move.y);
    }

    @ParameterizedTest
    @CsvSource({"0.5,0.5,0,0", "0.25,0.125,-8,-12", "1.0,0.0,16,-16"})
    void attackTargetIsSetToMouseCoordinates(
            double mouseX,
            double mouseY,
            double expectedX,
            double expectedY
    ) {
        Mouse mouse = new Mouse();
        mouse.position.x = mouseX;
        mouse.position.y = mouseY;
        world.registerResource(Mouse.class, mouse);

        final var cameraEntity = world.getEntityManager().createEntity();
        this.world.getEntityManager().addComponentTo(cameraEntity, new Transform());

        final var cameraProperties = new CameraProperties(cameraEntity);
        cameraProperties.viewportWidthInWorldUnits = 32.0f;
        cameraProperties.viewportHeightInWorldUnits = 32.0f;
        this.world.registerResource(CameraProperties.class, cameraProperties);

        this.world.getEntityManager().applyModifications();


        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(expectedX, abilities.targetPosition.x);
        assertEquals(expectedY, abilities.targetPosition.y);
    }

    @Test
    void havingInputAttackSetUpdatesAttack() {
        world.registerResource(new CameraProperties(null));
        Inputs inputs = this.world.fetchResource(Inputs.class);
        inputs.inputAttack = false;

        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();
        assertFalse(weaponInput.attack);

        inputs.inputAttack = true;
        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();
        assertTrue(weaponInput.attack);

        inputs.inputAttack = false;
        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();
        assertFalse(weaponInput.attack);
    }
}
