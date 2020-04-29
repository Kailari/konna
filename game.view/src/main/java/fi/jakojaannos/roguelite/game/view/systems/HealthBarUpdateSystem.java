package fi.jakojaannos.roguelite.game.view.systems;

import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.ui.*;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class HealthBarUpdateSystem implements ECSSystem {
    private final Vector2d tmpPosition = new Vector2d();
    private final Camera camera;
    private final UserInterface userInterface;
    private final Map<Integer, UIElement> healthBars = new HashMap<>();

    public HealthBarUpdateSystem(
            final Camera camera,
            final UserInterface userInterface
    ) {
        this.camera = camera;
        this.userInterface = userInterface;
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .withComponent(Health.class)
                    .withComponent(Transform.class)
                    .requireResource(CameraProperties.class)
                    .requireProvidedResource(TimeManager.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var cameraProperties = world.fetchResource(CameraProperties.class);

        final var entityManager = world.getEntityManager();
        final var timeManager = world.fetchResource(TimeManager.class);
        final var healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        for (final var uiElement : this.healthBars.values()) {
            // FIXME: Actually remove stale healthbars
            uiElement.setProperty(UIProperty.HIDDEN, true);
        }

        for (final var entity : (Iterable<Entity>) entities::iterator) {
            final var transform = entityManager.getComponentOf(entity, Transform.class)
                                               .orElseThrow();
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
                                                                     this.camera.getViewport()
                                                                                .getWidthInPixels(),
                                                                     this.camera.getViewport()
                                                                                .getHeightInPixels(),
                                                                     this.tmpPosition);
            this.tmpPosition.add(offsetX, offsetY);
            updateHealthBarFor(entity,
                               this.tmpPosition.x(),
                               this.tmpPosition.y(),
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
        uiElement.setProperty(UIProperty.LEFT, ProportionValue.absolute((int) x));
        uiElement.setProperty(UIProperty.TOP, ProportionValue.absolute((int) y));
        uiElement.setProperty(UIProperty.WIDTH, ProportionValue.absolute((int) width));
        uiElement.setProperty(UIProperty.HEIGHT, ProportionValue.absolute((int) height));
        uiElement.setProperty(UIProperty.PROGRESS, currentHealth);
        uiElement.setProperty(UIProperty.MAX_PROGRESS, maxHealth);
    }

    private UIElement createHealthBarForEntity(final Integer entityId) {
        return this.userInterface.addElement("healthbar#" + entityId,
                                             UIElementType.PROGRESS_BAR,
                                             builder -> { });
    }
}
