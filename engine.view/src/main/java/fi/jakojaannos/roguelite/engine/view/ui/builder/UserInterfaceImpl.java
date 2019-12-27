package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.data.components.EngineUIComponentGroups;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.resources.RenderPass;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIRoot;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.systems.ui.*;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import lombok.val;
import org.joml.Vector2d;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * The interface used to interact with the game.
 */
public class UserInterfaceImpl implements UserInterface {
    private final World uiWorld;
    private final SystemDispatcher uiDispatcher;

    public UserInterfaceImpl(
            final Viewport viewport,
            final SpriteBatch spriteBatch,
            final TextRenderer textRenderer
    ) {
        this.uiWorld = World.createNew(EntityManager.createNew(256, 32));
        this.uiWorld.getEntityManager().registerComponentGroup(EngineUIComponentGroups.ELEMENT_BOUND);

        this.uiDispatcher = SystemDispatcher.builder()
                                            .withGroups(UISystemGroups.values())
                                            .addGroupDependency(UISystemGroups.EVENTS, UISystemGroups.PREPARATIONS)
                                            .addGroupDependencies(UISystemGroups.RENDERING, UISystemGroups.PREPARATIONS, UISystemGroups.EVENTS)
                                            .withSystem(new UIHierarchySystem())
                                            .withSystem(new UILabelAutomaticSizeCalculationSystem(textRenderer))
                                            .withSystem(new UIElementBoundaryCalculationSystem())
                                            .withSystems(new UIPanelRenderingSystem(spriteBatch))
                                            .withSystems(new UILabelRenderingSystem(textRenderer))
                                            .build();

        this.uiWorld.createResource(UIRoot.class, new UIRoot(viewport));
        this.uiWorld.createResource(UIHierarchy.class, new UIHierarchy());
        this.uiWorld.createResource(RenderPass.class, new RenderPass());
    }

    public EntityManager getEntityManager() {
        return this.uiWorld.getEntityManager();
    }

    @Override
    public Queue<UIEvent> pollEvents(final Vector2d mousePos, boolean mouseClicked) {
        Queue<UIEvent> events = new ArrayDeque<>();
        this.uiWorld.getEntityManager()
                    .getEntitiesWith(ElementBoundaries.class)
                    .forEach(pair -> {
                        val name = this.uiWorld.getEntityManager()
                                               .getComponentOf(pair.getEntity(), Name.class)
                                               .orElseThrow().value;
                        val bounds = pair.getComponent();
                        if (mousePos.x > bounds.minX && mousePos.x < bounds.maxX && mousePos.y > bounds.minY && mousePos.y < bounds.maxY) {
                            if (mouseClicked) {
                                events.offer(new UIEvent(name, UIEvent.Type.CLICK));
                            }
                        }
                    });
        return events;
    }

    @Override
    public void render() {
        val renderPass = this.uiWorld.getOrCreateResource(RenderPass.class);

        for (renderPass.value = 0; renderPass.value < renderPass.maxHierarchyDepth; ++renderPass.value) {
            this.uiDispatcher.dispatch(this.uiWorld);
        }
    }
}
