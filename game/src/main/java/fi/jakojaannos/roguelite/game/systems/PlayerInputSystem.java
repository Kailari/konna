package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

public class PlayerInputSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(CharacterInput.class)
                    .withComponent(CharacterAbilities.class)
                    .withComponent(PlayerTag.class)
                    .requireResource(Inputs.class)
                    .requireResource(Mouse.class)
                    .requireResource(CameraProperties.class);
    }

    private final Vector2d tmpCursorPos = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val inputs = world.getOrCreateResource(Inputs.class);
        val mouse = world.getOrCreateResource(Mouse.class);
        val camProps = world.getOrCreateResource(CameraProperties.class);
        val cursorPosition = tmpCursorPos.set(0.0, 0.0);
        if (camProps.cameraEntity != null) {
            val camera = world.getEntityManager().getComponentOf(camProps.cameraEntity, Camera.class).get();
            mouse.calculateCursorPositionRelativeToCamera(camera, camProps, tmpCursorPos);
        } else {
            cursorPosition.set(mouse.pos.x * camProps.viewportWidthInWorldUnits,
                               mouse.pos.y * camProps.viewportHeightInWorldUnits);
        }

        val inputHorizontal = (inputs.inputRight ? 1 : 0) - (inputs.inputLeft ? 1 : 0);
        val inputVertical = (inputs.inputDown ? 1 : 0) - (inputs.inputUp ? 1 : 0);
        boolean inputAttack = inputs.inputAttack;

        entities.forEach(entity -> {
            val input = world.getEntityManager().getComponentOf(entity, CharacterInput.class).get();
            val abilities = world.getEntityManager().getComponentOf(entity, CharacterAbilities.class).get();
            input.move.set(inputHorizontal,
                           inputVertical);
            input.attack = inputAttack;
            abilities.attackTarget.set(cursorPosition);
        });
    }
}
