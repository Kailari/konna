package fi.jakojaannos.konna.view.adapters.gameplay;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.Without;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

/**
 * Renders any entities with transform as debug transform handles.
 */
public class CharacterHealthbarRenderAdapter implements EcsSystem<CharacterHealthbarRenderAdapter.Resources, CharacterHealthbarRenderAdapter.EntityData, EcsSystem.NoEvents> {
    private final UiElement healthbar;
    private final long healthbarDurationInTicks;

    public CharacterHealthbarRenderAdapter(
            final AssetManager assetManager,
            final long healthbarDurationInTicks
    ) {
        this.healthbarDurationInTicks = healthbarDurationInTicks;

        this.healthbar = assetManager.getStorage(UiElement.class)
                                     .getOrDefault("ui/healthbar.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var renderer = resources.renderer;
        final var timeManager = resources.timeManager;

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var health = entity.getData().health;

            final var isImportant = entity.hasComponent(PlayerTag.class);
            final var ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!isImportant && ticksSinceDamaged >= this.healthbarDurationInTicks) {
                return;
            }

            final var projection = resources.cameraProperties.projection;
            final var view = resources.cameraProperties.getViewMatrix();

            final var homogenous = new Vector4f((float) transform.position.x,
                                                (float) transform.position.y,
                                                0.0f,
                                                1.0f);
            final var transformation = projection.mul(view, new Matrix4f());
            homogenous.mul(transformation);

            final var clipSpaceX = homogenous.x / homogenous.w;
            final var clipSpaceY = homogenous.y / homogenous.w;

            final var healthPercentage = health.currentHealth / health.maxHealth;
            final var width = 0.1f * (float) healthPercentage;
            final var height = 0.01f;
            final var offsetX = -width / 2.0f;
            final var offsetY = 0.1f;

            renderer.ui().draw(this.healthbar,
                               clipSpaceX + offsetX,
                               clipSpaceY + offsetY,
                               width,
                               height);
        });
    }

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager,
            CameraProperties cameraProperties
    ) {}

    public static record EntityData(
            Transform transform,
            Health health,
            @Without NoDrawTag noDraw
    ) {}
}
