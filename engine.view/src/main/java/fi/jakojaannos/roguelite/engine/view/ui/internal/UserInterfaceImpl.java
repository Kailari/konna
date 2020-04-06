package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.function.Consumer;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
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
    private final ECSSystem hierarchySystem;

    @Override
    public int getWidth() {
        return this.viewport.getWidthInPixels();
    }

    @Override
    public int getHeight() {
        return this.viewport.getWidthInPixels();
    }

    public EntityManager getEntityManager() {
        return this.uiWorld.getEntityManager();
    }

    @Override
    public Stream<UIElement> getRoots() {
        return this.uiWorld.getResource(UIHierarchy.class)
                           .getRoots();
    }

    public UserInterfaceImpl(
            final Viewport viewport,
            final TextSizeProvider textSizeProvider
    ) {
        this.viewport = viewport;
        this.uiWorld = World.createNew();
        this.uiWorld.registerResource(Mouse.class, new Mouse());

        final var builder = SystemDispatcher.builder();
        final var preparations = builder.group("preparations")
                                        .withSystem(this.hierarchySystem = new UIHierarchySystem())
                                        .withSystem(new UILabelAutomaticSizeCalculationSystem(textSizeProvider))
                                        .withSystem(new UIElementBoundaryCalculationSystem())
                                        .buildGroup();

        final var events = builder.group("events")
                                  .withSystem(new UIElementHoverEventProvider())
                                  .withSystem(new UIElementClickEventProvider())
                                  .dependsOn(preparations)
                                  .buildGroup();

        builder.group("cleanup")
               .dependsOn(preparations, events)
               .buildGroup();

        this.uiDispatcher = builder.build();
        this.uiWorld.provideResource(UIRoot.class, new UIRoot(viewport));
        this.uiWorld.provideResource(UIHierarchy.class, new UIHierarchy());
    }

    @Override
    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>> UIElement addElement(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        final var elementEntity = this.uiWorld.getEntityManager().createEntity();
        final var builder = elementType.getBuilder(this,
                                                   elementEntity,
                                                   name,
                                                   component -> this.uiWorld.getEntityManager()
                                                                            .addComponentTo(elementEntity, component));
        factory.accept(builder);
        final var element = this.uiWorld.getResource(UIHierarchy.class)
                                        .getOrCreateElementFor(elementEntity, this.uiWorld.getEntityManager());
        element.setProperty(UIProperty.TYPE, elementType);
        this.uiWorld.getEntityManager().applyModifications();
        this.updateHierarchy();
        return element;
    }

    @Override
    public Stream<UIElement> allElements() {
        return this.uiWorld.getResource(UIHierarchy.class)
                           .getElements();
    }

    @Override
    public void update(final TimeManager time, final Mouse mouse, final Events events) {
        this.uiWorld.getEntityManager().applyModifications();
        this.uiWorld.provideResource(Time.class, new Time(time));
        this.uiWorld.provideResource(Events.class, events);
        final var uiMouse = this.uiWorld.fetchResource(Mouse.class);
        uiMouse.clicked = mouse.clicked;
        uiMouse.position.set(mouse.position);

        this.uiDispatcher.tick(this.uiWorld);
        this.uiWorld.getEntityManager().applyModifications();
    }

    public void updateHierarchy() {
        this.hierarchySystem.tick(this.uiWorld.getEntityManager().getAllEntities(),
                                  this.uiWorld);
    }
}
