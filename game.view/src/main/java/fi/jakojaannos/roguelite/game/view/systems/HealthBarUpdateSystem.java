package fi.jakojaannos.roguelite.game.view.systems;

import lombok.RequiredArgsConstructor;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

@RequiredArgsConstructor
public class HealthBarUpdateSystem implements ECSSystem {
    private final Vector2d tmpPosition = new Vector2d();
    private final Camera camera;
    private final UserInterface userInterface;
    private final Map<Integer, UIElement> healthBars = new HashMap<>();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .withComponent(Health.class)
                    .withComponent(Transform.class)
                    .requireResource(CameraProperties.class)
                    .requireProvidedResource(Time.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var cameraProperties = world.getOrCreateResource(CameraProperties.class);

        final var entityManager = world.getEntityManager();
        final var timeManager = world.getResource(Time.class);
        final var healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        for (final var uiElement : this.healthBars.values()) {
            // FIXME: Actually remove stale healthbars
            uiElement.setProperty(UIProperty.HIDDEN, true);
        }

        for (final var entity : (Iterable<Entity>) entities::iterator) {
            final var transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            final var health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            final var isImportant = entityManager.hasComponent(entity, PlayerTag.class);
            final var ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!isImportant && ticksSinceDamaged >= healthbarDurationInTicks) {
                continue;
            }

            // TODO: Make healthbar w/h configurable on per-entity type basis
            final var width = 1.5f * (float) this.camera.getPixelsPerUnitX();
            final var height = 0.25f * (float) this.camera.getPixelsPerUnitY();
            final var offsetX = -width / 2.0f;
            final var offsetY = (float) this.camera.getPixelsPerUnitY() * 0.85f;
            cameraProperties.calculateRelativePositionAndReMapToSize(transform.position,
                                                                     entityManager,
                                                                     this.camera.getViewport().getWidthInPixels(),
                                                                     this.camera.getViewport().getHeightInPixels(),
                                                                     tmpPosition);
            tmpPosition.add(offsetX, offsetY);
            updateHealthBarFor(entity,
                               tmpPosition.x(),
                               tmpPosition.y(),
                               width,
                               height,
                               health.currentHealth,
                               health.maxHealth);
        }
    }

    private void updateHealthBarFor(
            final Entity entity,
            final double x,
            final double y,
            final double width,
            final double height,
            final double currentHealth,
            final double maxHealth
    ) {
        final var uiElement = this.healthBars.computeIfAbsent(entity.getId(), this::createHealthBarForEntity);
        uiElement.setProperty(UIProperty.HIDDEN, false);
        uiElement.setProperty(UIProperty.MIN_X, (int) x);
        uiElement.setProperty(UIProperty.MIN_Y, (int) y);
        uiElement.setProperty(UIProperty.WIDTH, (int) width);
        uiElement.setProperty(UIProperty.HEIGHT, (int) height);
        uiElement.setProperty(UIProperty.PROGRESS, currentHealth);
        uiElement.setProperty(UIProperty.MAX_PROGRESS, maxHealth);
    }

    private UIElement createHealthBarForEntity(final Integer entityId) {
        return this.userInterface.addElement("healthbar#" + entityId,
                                             UIElementType.PROGRESS_BAR,
                                             builder -> {});
    }
}
