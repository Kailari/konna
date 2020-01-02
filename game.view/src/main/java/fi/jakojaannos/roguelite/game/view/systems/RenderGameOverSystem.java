package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Stream;

// TODO: Use UI

@RequiredArgsConstructor
public class RenderGameOverSystem implements ECSSystem {
    private static final String GAME_OVER_MESSAGE = "You Suck.";
    private static final String HELP_TEXT = "Press <SPACE> to restart";

    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class)
                    .withComponent(PlayerTag.class);
    }

    private final TextRenderer textRenderer;
    private final Camera camera;
    private final Font font;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val anyPlayerAlive = entities.count() > 0;
        if (anyPlayerAlive) {
            return;
        }

        this.camera.useScreenCoordinates();
        val halfScreenWidth = this.camera.getViewport().getWidthInPixels() / 2.0;
        val halfScreenHeight = this.camera.getViewport().getHeightInPixels() / 2.0;
        this.textRenderer.drawCenteredOnScreen(halfScreenWidth, halfScreenHeight, 48, font, GAME_OVER_MESSAGE);
        this.textRenderer.drawCenteredOnScreen(halfScreenWidth, halfScreenHeight + 50, 24, font, HELP_TEXT);
    }
}
