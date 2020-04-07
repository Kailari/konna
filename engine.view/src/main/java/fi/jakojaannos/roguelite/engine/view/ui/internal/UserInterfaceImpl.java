package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.EventBus;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.systems.ui.*;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;

/**
 * The interface used to interact with the game.
 */
public class UserInterfaceImpl implements UserInterface {
    private final World uiWorld;
    private final SystemDispatcher uiDispatcher;
    private final Viewport viewport;
    private final UIHierarchy hierarchy;

    private List<EntityHandle> elementEntities = new ArrayList<>();

    @Override
    public int getWidth() {
        return this.viewport.getWidthInPixels();
    }

    @Override
    public int getHeight() {
        return this.viewport.getWidthInPixels();
    }

    public World getWorld() {
        return this.uiWorld;
    }

    @Override
    public Stream<UIElement> getRoots() {
        return this.uiWorld.fetchResource(UIHierarchy.class)
                           .getRoots();
    }

    public UserInterfaceImpl(
            final Events events,
            final Viewport viewport,
            final TextSizeProvider textSizeProvider
    ) {
        this.viewport = viewport;
        this.uiWorld = World.createNew();
        this.hierarchy = new UIHierarchy();
        this.uiWorld.registerResource(UIHierarchy.class, this.hierarchy);
        this.uiWorld.registerResource(Mouse.class, new Mouse());
        this.uiWorld.registerResource(UIEventBus.class, ((EventBus<UIEvent>) events.ui())::fire);

        final var builder = SystemDispatcher.builder();
        final var preparations = builder.group("preparations")
                                        .withSystem(new UIHierarchySystem())
                                        .withSystem(new UILabelAutomaticSizeCalculationSystem(textSizeProvider))
                                        .withSystem(new UIElementBoundaryCalculationSystem())
                                        .buildGroup();

        final var uiEvents = builder.group("ui-events")
                                    .withSystem(new UIElementHoverEventProvider())
                                    .withSystem(new UIElementClickEventProvider())
                                    .dependsOn(preparations)
                                    .buildGroup();

        builder.group("cleanup")
               .dependsOn(preparations, uiEvents)
               .buildGroup();

        this.uiDispatcher = builder.build();

        this.uiWorld.registerResource(UIRoot.class, new UIRoot(viewport));
    }

    @Override
    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>>
    UIElement addElement(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        final var elementEntity = this.uiWorld.createEntity();
        this.elementEntities.add(elementEntity);
        factory.accept(elementType.getBuilder(this, elementEntity, name));
        final var element = this.hierarchy.getOrCreateElementFor(elementEntity);
        element.setProperty(UIProperty.TYPE, elementType);
        this.uiWorld.commitEntityModifications();
        this.updateHierarchy();
        return element;
    }

    @Override
    public Stream<UIElement> allElements() {
        return this.uiWorld.fetchResource(UIHierarchy.class)
                           .getElements();
    }

    @Override
    public void update(final TimeManager time, final Mouse mouse, final Events events) {
        this.uiWorld.commitEntityModifications();
        this.elementEntities = this.elementEntities.stream()
                                                   .filter(Predicate.not(EntityHandle::isDestroyed))
                                                   .filter(Predicate.not(EntityHandle::isPendingRemoval))
                                                   .collect(Collectors.toCollection(ArrayList::new));

        this.uiWorld.provideResource(Events.class, events); // FIXME: Resources should be registered once

        final var uiMouse = this.uiWorld.fetchResource(Mouse.class);
        uiMouse.clicked = mouse.clicked;
        uiMouse.position.set(mouse.position);

        this.uiDispatcher.tick(this.uiWorld);
        this.uiWorld.commitEntityModifications();
    }

    // TODO: Figure out where and why this is necessary and get rid of the root cause
    public void updateHierarchy() {
        this.elementEntities = this.elementEntities.stream()
                                                   .filter(Predicate.not(EntityHandle::isDestroyed))
                                                   .filter(Predicate.not(EntityHandle::isPendingRemoval))
                                                   .collect(Collectors.toCollection(ArrayList::new));
        this.elementEntities.forEach(entity -> this.hierarchy.update(entity,
                                                                     UIHierarchySystem.getParent(entity)));
    }
}
