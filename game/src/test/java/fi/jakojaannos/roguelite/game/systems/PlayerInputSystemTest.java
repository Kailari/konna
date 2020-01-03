package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInputSystemTest {
    private PlayerInputSystem system;
    private World world;
    private CharacterInput input;
    private Entity player;
    private CharacterAbilities abilities;

    @BeforeEach
    void beforeEach() {
        system = new PlayerInputSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        player = entityManager.createEntity();
        this.input = new CharacterInput();
        this.abilities = new CharacterAbilities();
        entityManager.addComponentTo(player, this.input);
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
        Inputs inputs = this.world.getOrCreateResource(Inputs.class);
        inputs.inputLeft = left;
        inputs.inputRight = right;
        inputs.inputUp = up;
        inputs.inputDown = down;
        system.tick(Stream.of(player), this.world);
        world.getEntityManager().applyModifications();

        assertEquals(expectedHorizontal, this.input.move.x);
        assertEquals(expectedVertical, this.input.move.y);
    }

    @ParameterizedTest
    @CsvSource({"0.5,0.5,16,16", "0.25,0.125,8,4", "1.0,0.0,32,0"})
    void attackTargetIsSetToMouseCoordinates(
            double mouseX,
            double mouseY,
            double expectedX,
            double expectedY
    ) {
        Mouse mouse = this.world.getOrCreateResource(Mouse.class);
        CameraProperties camBounds = this.world.getOrCreateResource(CameraProperties.class);
        camBounds.viewportWidthInWorldUnits = 32.0f;
        camBounds.viewportHeightInWorldUnits = 32.0f;
        mouse.position.x = mouseX;
        mouse.position.y = mouseY;

        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(expectedX, abilities.attackTarget.x);
        assertEquals(expectedY, abilities.attackTarget.y);
    }

    @Test
    void havingInputAttackSetUpdatesAttack() {
        Inputs inputs = this.world.getOrCreateResource(Inputs.class);
        inputs.inputAttack = false;

        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();
        assertFalse(input.attack);

        inputs.inputAttack = true;
        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();
        assertTrue(input.attack);

        inputs.inputAttack = false;
        system.tick(Stream.of(player), this.world);
        this.world.getEntityManager().applyModifications();
        assertFalse(input.attack);
    }
}
