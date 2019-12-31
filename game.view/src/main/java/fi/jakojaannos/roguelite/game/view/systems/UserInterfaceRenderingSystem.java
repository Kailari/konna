package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.resources.GameStateManager;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterfaceRenderer;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

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
        val mouse = world.getOrCreateResource(Mouse.class);
        val viewport = this.camera.getViewport();
        val mousePos = mouse.pos.mul(viewport.getWidthInPixels(),
                                     viewport.getHeightInPixels(),
                                     new Vector2d());
        // FIXME: Updating should not happen in the renderer
        val events = this.userInterface.update(mousePos,
                                               world.getOrCreateResource(Inputs.class).inputAttack);
        while (!events.isEmpty()) {
            val event = events.remove();
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
