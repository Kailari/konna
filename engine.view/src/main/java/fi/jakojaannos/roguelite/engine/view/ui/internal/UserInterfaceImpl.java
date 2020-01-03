package fi.jakojaannos.roguelite.engine.view.ui.internal;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.Name;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.systems.ui.*;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;
import lombok.val;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The interface used to interact with the game.
 */
public class UserInterfaceImpl implements UserInterface {
    private final World uiWorld;
    private final SystemDispatcher uiDispatcher;
    private final Viewport viewport;
    private final ECSSystem hierarchySystem;

    @Override
    public int getWidth() {
        return this.viewport.getWidthInPixels();
    }

    @Override
    public int getHeight() {
        return this.viewport.getWidthInPixels();
    }

    public UserInterfaceImpl(
            final Viewport viewport,
            final TextSizeProvider textSizeProvider
    ) {
        this.viewport = viewport;
        this.uiWorld = World.createNew(EntityManager.createNew(256, 32));

        this.uiDispatcher = SystemDispatcher.builder()
                                            .withGroups(UISystemGroups.values())
                                            .addGroupDependency(UISystemGroups.EVENTS, UISystemGroups.PREPARATIONS)
                                            .withSystem(this.hierarchySystem = new UIHierarchySystem())
                                            .withSystem(new UILabelAutomaticSizeCalculationSystem(textSizeProvider))
                                            .withSystem(new UIElementBoundaryCalculationSystem())
                                            .withSystem(new UIElementHoverEventProvider())
                                            .withSystem(new UIElementClickEventProvider())
                                            .build();

        this.uiWorld.createOrReplaceResource(UIRoot.class, new UIRoot(viewport));
        this.uiWorld.createOrReplaceResource(UIHierarchy.class, new UIHierarchy());
    }

    public EntityManager getEntityManager() {
        return this.uiWorld.getEntityManager();
    }

    @Override
    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        val elementEntity = this.uiWorld.getEntityManager().createEntity();
        val builder = elementType.getBuilder(this,
                                             elementEntity,
                                             name,
                                             component -> this.uiWorld.getEntityManager().addComponentTo(elementEntity, component));
        factory.accept(builder);
        val element = this.uiWorld.getOrCreateResource(UIHierarchy.class).getOrCreateElementFor(elementEntity, this.uiWorld.getEntityManager());
        element.setProperty(UIProperty.TYPE, elementType);
        return element;
    }

    @Override
    public Stream<UIElement> getRoots() {
        return this.uiWorld.getOrCreateResource(UIHierarchy.class)
                           .getRoots();
    }

    @Override
    public void update(final TimeManager time, final Mouse mouse, final Events events) {
        this.uiWorld.createOrReplaceResource(Time.class, new Time(time));
        this.uiWorld.createOrReplaceResource(Events.class, events);
        this.uiWorld.createOrReplaceResource(Mouse.class, mouse);

        this.uiDispatcher.dispatch(this.uiWorld);
    }

    public void updateHierarchy() {
        this.hierarchySystem.tick(this.uiWorld.getEntityManager().getAllEntities(),
                                  this.uiWorld);
    }
}
