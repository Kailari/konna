package fi.jakojaannos.roguelite.game.view.systems;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.rendering.ui.UserInterfaceRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UserInterfaceRenderingSystem implements ECSSystem, AutoCloseable {
    private final UserInterface userInterface;
    private final Camera camera;
    private final UserInterfaceRenderer userInterfaceRenderer;

    public UserInterfaceRenderingSystem(
            final Path assetRoot,
            final Camera camera,
            final AssetRegistry<Font> fontRegistry,
            final AssetRegistry<Sprite> spriteRegistry,
            final TextRenderer textRenderer,
            final UserInterface userInterface,
            final RenderingBackend backend
    ) {
        this.userInterface = userInterface;
        this.camera = camera;
        this.userInterfaceRenderer = new UserInterfaceRenderer(assetRoot,
                                                               spriteRegistry,
                                                               textRenderer,
                                                               fontRegistry,
                                                               backend,
                                                               userInterface.getRoot());
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .requireResource(Mouse.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var rawMouse = world.fetchResource(Mouse.class);
        final var mouse = new Mouse();
        mouse.clicked = rawMouse.clicked;
        mouse.position.set(rawMouse.position)
                      .mul(this.camera.getViewport().getWidthInPixels(),
                           this.camera.getViewport().getHeightInPixels());
        this.userInterface.update(mouse);

        this.camera.useScreenCoordinates();
        this.userInterfaceRenderer.render(this.userInterface);
    }

    @Override
    public void close() throws Exception {
        this.userInterfaceRenderer.close();
    }
}
