package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.rendering.UserInterfaceRenderer;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class UserInterfaceRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
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
        val uiEvents = events.getUi();
        while (uiEvents.hasEvents()) {
            val event = uiEvents.pollEvent();
            if (event.getElement().equalsIgnoreCase("play_button") && event.getType() == UIEvent.Type.CLICK) {
                world.getOrCreateResource(GameStateManager.class)
                     .queueStateChange(new GameplayGameState(System.nanoTime(),
                                                             World.createNew(EntityManager.createNew(256, 32)),
                                                             world.getOrCreateResource(Time.class).getTimeManager()));
            }
        }

        this.camera.useScreenCoordinates();
        this.userInterfaceRenderer.render(this.userInterface);
    }
}
