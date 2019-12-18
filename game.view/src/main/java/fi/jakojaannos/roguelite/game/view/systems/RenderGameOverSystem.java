package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.game.data.components.PlayerTag;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Stream;

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
    private final LWJGLCamera camera;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val anyPlayerAlive = entities.count() > 0;
        if (anyPlayerAlive) {
            return;
        }

        renderCentered(0, 48, GAME_OVER_MESSAGE);
        renderCentered(50, 24, HELP_TEXT);
    }

    private void renderCentered(final double yOffset, final int fontSize, final String text) {
        val screenWidth = this.camera.getViewportWidthInPixels();
        val screenHeight = this.camera.getViewportHeightInPixels();
        val textWidth = this.textRenderer.getStringWidthInPixels(fontSize, text);

        val textX = (screenWidth - textWidth) / 2;
        val textY = (screenHeight - fontSize) / 2;
        this.textRenderer.drawOnScreen(textX, textY + yOffset, fontSize, text);
    }
}
