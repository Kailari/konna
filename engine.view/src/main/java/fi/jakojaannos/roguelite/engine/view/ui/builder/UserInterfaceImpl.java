package fi.jakojaannos.roguelite.engine.view.ui.builder;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.data.components.EngineUIComponentGroups;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIRoot;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIElementBoundaryCalculationSystem;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIHierarchySystem;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIPanelRenderingSystem;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UISystemGroups;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

/**
 * The interface used to interact with the game.
 */
public class UserInterfaceImpl<TTexture extends Texture> implements UserInterface<TTexture> {
    private final World uiWorld;
    private final SystemDispatcher uiDispatcher;

    public UserInterfaceImpl(
            final Viewport viewport,
            final SpriteBatch<TTexture> spriteBatch,
            final SpriteRegistry<TTexture> spriteRegistry
    ) {
        this.uiWorld = World.createNew(EntityManager.createNew(256, 32));
        this.uiWorld.getEntityManager().registerComponentGroup(EngineUIComponentGroups.ELEMENT_BOUND);

        this.uiDispatcher = SystemDispatcher.builder()
                                            .withGroups(UISystemGroups.values())
                                            .addGroupDependency(UISystemGroups.RENDERING, UISystemGroups.PREPARATIONS)
                                            .withSystem(new UIHierarchySystem())
                                            .withSystem(new UIElementBoundaryCalculationSystem())
                                            .withSystems(new UIPanelRenderingSystem<>(spriteBatch, spriteRegistry))
                                            .build();

        this.uiWorld.createResource(UIRoot.class, new UIRoot(viewport));
        this.uiWorld.createResource(UIHierarchy.class, new UIHierarchy());
    }

    public EntityManager getEntityManager() {
        return this.uiWorld.getEntityManager();
    }

    @Override
    public void render() {
        this.uiDispatcher.dispatch(this.uiWorld);
    }
}
