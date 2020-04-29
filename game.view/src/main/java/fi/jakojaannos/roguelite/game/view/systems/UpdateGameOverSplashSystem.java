package fi.jakojaannos.roguelite.game.view.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class UpdateGameOverSplashSystem implements ECSSystem {
    private final UIElement gameOverSplashElement;

    public UpdateGameOverSplashSystem(final UserInterface userInterface) {
        this.gameOverSplashElement =
                userInterface.findElements(that -> that.hasName().equalTo("game-over-container"))
                             .findFirst()
                             .orElseThrow();
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .withComponent(PlayerTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var anyPlayerAlive = entities.count() > 0;
        this.gameOverSplashElement.setProperty(UIProperty.HIDDEN, anyPlayerAlive);
    }
}
