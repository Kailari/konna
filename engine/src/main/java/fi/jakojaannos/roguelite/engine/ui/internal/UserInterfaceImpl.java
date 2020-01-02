package fi.jakojaannos.roguelite.engine.ui.internal;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.Name;
import fi.jakojaannos.roguelite.engine.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.systems.ui.UIElementBoundaryCalculationSystem;
import fi.jakojaannos.roguelite.engine.systems.ui.UIHierarchySystem;
import fi.jakojaannos.roguelite.engine.systems.ui.UILabelAutomaticSizeCalculationSystem;
import fi.jakojaannos.roguelite.engine.systems.ui.UISystemGroups;
import fi.jakojaannos.roguelite.engine.ui.*;
import fi.jakojaannos.roguelite.engine.ui.builder.UIElementBuilder;
import lombok.val;
import org.joml.Vector2d;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The interface used to interact with the game.
 */
public class UserInterfaceImpl implements UserInterface {
    private final World uiWorld;
    private final SystemDispatcher uiDispatcher;

    public UserInterfaceImpl(
            final ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider textSizeProvider
    ) {
        this.uiWorld = World.createNew(EntityManager.createNew(256, 32));

        this.uiDispatcher = SystemDispatcher.builder()
                                            .withGroups(UISystemGroups.values())
                                            .addGroupDependency(UISystemGroups.EVENTS, UISystemGroups.PREPARATIONS)
                                            .withSystem(new UIHierarchySystem())
                                            .withSystem(new UILabelAutomaticSizeCalculationSystem(textSizeProvider))
                                            .withSystem(new UIElementBoundaryCalculationSystem())
                                            .build();

        this.uiWorld.createResource(UIRoot.class, new UIRoot(viewportSizeProvider));
        this.uiWorld.createResource(UIHierarchy.class, new UIHierarchy());
    }

    public EntityManager getEntityManager() {
        return this.uiWorld.getEntityManager();
    }

    @Override
    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            final String name, final T elementType, final Consumer<TBuilder> factory
    ) {
        val elementEntity = this.uiWorld.getEntityManager().createEntity();
        val builder = elementType.getBuilder(this,
                                             elementEntity,
                                             name,
                                             component -> this.uiWorld.getEntityManager().addComponentTo(elementEntity, component));
        factory.accept(builder);
        return this.uiWorld.getOrCreateResource(UIHierarchy.class).getOrCreateElementFor(elementEntity, this.uiWorld.getEntityManager());
    }

    @Override
    public Stream<UIElement> getRoots() {
        return this.uiWorld.getOrCreateResource(UIHierarchy.class)
                           .getRoots();
    }

    @Override
    public Queue<UIEvent> update(final Vector2d mousePos, boolean mouseClicked) {
        this.uiDispatcher.dispatch(this.uiWorld);

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
}
