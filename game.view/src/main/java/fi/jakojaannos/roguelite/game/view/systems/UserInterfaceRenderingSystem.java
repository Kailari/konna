package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.rendering.UserInterfaceRenderer;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class UserInterfaceRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .requireResource(Mouse.class)
                    .requireResource(Events.class)
                    .requireResource(Time.class);
    }

    private final UserInterface userInterface;

    private final Camera camera;
    private final UserInterfaceRenderer userInterfaceRenderer;

    public UserInterfaceRenderingSystem(
            final Camera camera,
            final AssetRegistry<Font> fontRegistry,
            final AssetRegistry<Sprite> spriteRegistry,
            final SpriteBatch spriteBatch,
            final TextRenderer textRenderer,
            final UserInterface userInterface
    ) {
        this.userInterface = userInterface;
        this.camera = camera;
        this.userInterfaceRenderer = new UserInterfaceRenderer(spriteBatch, spriteRegistry, textRenderer, fontRegistry);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val rawMouse = world.getOrCreateResource(Mouse.class);
        val mouse = new Mouse();
        mouse.clicked = rawMouse.clicked;
        mouse.position.set(rawMouse.position)
                      .mul(this.camera.getViewport().getWidthInPixels(),
                           this.camera.getViewport().getHeightInPixels());
        val events = world.getOrCreateResource(Events.class);
        this.userInterface.update(world.getOrCreateResource(Time.class).getTimeManager(),
                                  mouse,
                                  events);

        this.camera.useScreenCoordinates();
        this.userInterfaceRenderer.render(this.userInterface);
    }
}
