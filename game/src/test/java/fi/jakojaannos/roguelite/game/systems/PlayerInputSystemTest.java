package fi.jakojaannos.roguelite.game.systems;

import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;

import static fi.jakojaannos.roguelite.engine.ecs.ComponentFactory.factory;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.*;

class PlayerInputSystemTest {
    private MovementInput movementInput;
    private WeaponInput weaponInput;
    private AttackAbility abilities;

    void beforeEach(final World world) {
        world.registerResource(new Inputs());
        world.registerResource(new Mouse());

        final var cameraProperties = new CameraProperties();
        cameraProperties.setPosition(new Vector3f(0.0f, 0.0f, 25.0f));
        cameraProperties.setRotation(new Quaternionf());

        world.registerResource(cameraProperties);

        world.createEntity(movementInput = new MovementInput(),
                           weaponInput = new WeaponInput(),
                           new PlayerTag(),
                           factory(entity -> this.abilities = new AttackAbility(new DamageSource.Entity(entity),
                                                                                CollisionLayer.PLAYER,
                                                                                0.0,
                                                                                0.0,
                                                                                0)));
    }

    @ParameterizedTest
    @CsvSource({
                       "0.0f,0.0f,false,false,false,false",
                       "-1.0f,0.0f,true,false,false,false",
                       "1.0f,0.0f,false,true,false,false",
                       "0.0f,1.0f,false,false,true,false",
                       "0.0f,-1.0f,false,false,false,true",
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
        whenGame().withSystems(new PlayerInputSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      final var inputs = world.fetchResource(Inputs.class);
                      inputs.inputLeft = left;
                      inputs.inputRight = right;
                      inputs.inputUp = up;
                      inputs.inputDown = down;
                  })
                  .runsSingleTick()
                  .expect(state -> assertEquals(new Vector2d(expectedHorizontal, expectedVertical),
                                                this.movementInput.move));
    }

    @ParameterizedTest
    @CsvSource({"0.5,0.5,12.5,12.5", "0.25,0.125,6.25,3.125", "1.0,0.0,25.0,0"})
    void attackTargetIsSetToMouseCoordinates(
            double mouseX,
            double mouseY,
            double expectedX,
            double expectedY
    ) {
        whenGame().withSystems(new PlayerInputSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      Mouse mouse = world.fetchResource(Mouse.class);
                      mouse.position.x = mouseX;
                      mouse.position.y = mouseY;
                  })
                  .runsSingleTick()
                  .expect(state -> assertEquals(new Vector2d(expectedX, expectedY),
                                                abilities.targetPosition));
    }

    @Test
    void havingInputAttackSetUpdatesAttack() {
        whenGame().withSystems(new PlayerInputSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      final var inputs = world.fetchResource(Inputs.class);
                      inputs.inputAttack = false;
                  })
                  .runsSingleTick()
                  .expect(state -> assertFalse(weaponInput.attack))
                  .then(state -> {
                      final var inputs = state.world().fetchResource(Inputs.class);
                      inputs.inputAttack = true;
                  })
                  .runsSingleTick()
                  .expect(state -> assertTrue(weaponInput.attack))
                  .then(state -> {
                      final var inputs = state.world().fetchResource(Inputs.class);
                      inputs.inputAttack = false;
                  })
                  .runsSingleTick()
                  .expect(state -> assertFalse(weaponInput.attack));
    }
}
