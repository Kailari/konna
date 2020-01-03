package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import lombok.val;

import java.util.stream.Stream;

public class UpdateGameOverSplashSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .withComponent(PlayerTag.class);
    }

    private final UIElement gameOverSplashElement;

    public UpdateGameOverSplashSystem(final UserInterface userInterface) {
        this.gameOverSplashElement = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("game-over-container"))
                                                  .findFirst()
                                                  .orElseThrow();
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val anyPlayerAlive = entities.count() > 0;
        this.gameOverSplashElement.setProperty(UIProperty.HIDDEN, anyPlayerAlive);
    }
}
