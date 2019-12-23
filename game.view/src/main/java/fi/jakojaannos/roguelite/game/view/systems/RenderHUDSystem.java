package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;

import java.util.stream.Stream;

public class RenderHUDSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class);
    }

    private final LWJGLTextRenderer textRenderer;

    public RenderHUDSystem(final LWJGLTextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.textRenderer.drawOnScreen(0, 0, 24, "This is some test text\n(on-screen)");
        this.textRenderer.drawInWorld(-12, -5, 24, "This is some test text\n(in-world)");
    }
}
