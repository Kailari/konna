package fi.jakojaannos.roguelite.game.view.systems;

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
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HealthBarUpdateSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .withComponent(Health.class)
                    .withComponent(Transform.class)
                    .requireResource(CameraProperties.class)
                    .requireResource(Time.class);
    }

    private final Vector2d tmpPosition = new Vector2d();

    private final Camera camera;
    private final UserInterface userInterface;
    private final Map<Integer, UIElement> healthBars = new HashMap<>();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val cameraProperties = world.getOrCreateResource(CameraProperties.class);

        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);
        val healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        for (val uiElement : this.healthBars.values()) {
            // FIXME: Actually remove stale healthbars
            uiElement.setProperty(UIProperty.HIDDEN, true);
        }

        for (val entity : (Iterable<Entity>) entities::iterator) {
            val transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            val health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            long ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!health.healthBarAlwaysVisible && ticksSinceDamaged >= healthbarDurationInTicks) {
                continue;
            }

            // TODO: Make healthbar w/h configurable on per-entity type basis
            val width = 1.5f * (float) this.camera.getPixelsPerUnitX();
            val height = 0.25f * (float) this.camera.getPixelsPerUnitY();
            val offsetX = -width / 2.0f;
            val offsetY = (float) this.camera.getPixelsPerUnitY() * 0.85f;
            cameraProperties.calculateRelativePositionAndReMapToSize(transform.position,
                                                                     entityManager,
                                                                     this.camera.getViewport().getWidthInPixels(),
                                                                     this.camera.getViewport().getHeightInPixels(),
                                                                     tmpPosition);
            tmpPosition.add(offsetX, offsetY);
            updateHealthBarFor(entity, tmpPosition.x(), tmpPosition.y(), width, height, health.currentHealth, health.maxHealth);
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
        val uiElement = this.healthBars.computeIfAbsent(entity.getId(), this::createHealthBarForEntity);
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
