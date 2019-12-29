package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class RenderHUDSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class);
    }

    private final LWJGLTextRenderer textRenderer;
    private final Font font;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.textRenderer.drawOnScreen(0, 0, 24, font, "This is some test text\n(on-screen)");
        this.textRenderer.drawInWorld(-12, -5, 24, font, "This is some test text\n(in-world)");
    }
}
