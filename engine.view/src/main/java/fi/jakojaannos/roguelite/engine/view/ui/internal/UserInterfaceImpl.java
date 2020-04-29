package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.EventBus;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIElementBoundaryResolver;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIElementClickEventProvider;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIElementHoverEventProvider;
import fi.jakojaannos.roguelite.engine.view.systems.ui.UIElementLabelSizeResolver;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIElementBuilder;

/**
 * The interface used to interact with the game.
 */
public class UserInterfaceImpl implements UserInterface {
    private final TimeManager timeManager;
    private final Viewport viewport;
    private final UIRoot uiRoot;

    private final Set<UIElement> roots = new HashSet<>();
    private final List<UIElement> allElements = new ArrayList<>();
    private final UIEventBus eventBus;

    private final UIElementLabelSizeResolver labelSizeResolver;
    private final UIElementBoundaryResolver boundaryResolver;
    private final UIElementHoverEventProvider hoverEventProvider;
    private final UIElementClickEventProvider clickEventProvider;

    private final AtomicInteger idCounter = new AtomicInteger(0);

    @SuppressWarnings("rawtypes")
    private final Map<UIProperty, UIPropertyContainer> propertyContainers = new HashMap<>();

    @Override
    public int getWidth() {
        return this.viewport.getWidthInPixels();
    }

    @Override
    public int getHeight() {
        return this.viewport.getWidthInPixels();
    }

    @Override
    public Stream<UIElement> getRoots() {
        return this.roots.stream();
    }

    @Override
    public UIRoot getRoot() {
        return this.uiRoot;
    }

    public UserInterfaceImpl(
            final Events events,
            final TimeManager timeManager,
            final Viewport viewport,
            final TextSizeProvider textSizeProvider
    ) {
        this.timeManager = timeManager;
        this.viewport = viewport;
        this.uiRoot = new UIRoot(viewport);

        // HACK: We *know* that the `events.ui()` is actually an event bus. Its fire method is
        //       compatible with UIEventBus signature so use cast + method reference to convert.
        this.eventBus = ((EventBus<UIEvent>) events.ui())::fire;

        this.labelSizeResolver = new UIElementLabelSizeResolver(textSizeProvider, this.uiRoot);
        this.boundaryResolver = new UIElementBoundaryResolver(this.uiRoot);
        this.clickEventProvider = new UIElementClickEventProvider();
        this.hoverEventProvider = new UIElementHoverEventProvider();
    }

    @Override
    public <T extends UIElementType<TBuilder>, TBuilder extends UIElementBuilder<TBuilder>>
    UIElement addElement(
            final String name,
            final T elementType,
            final Consumer<TBuilder> factory
    ) {
        final var element = new UIElementImpl(this.idCounter.getAndIncrement(), this);
        final var builder = elementType.getBuilder(this, element, name);
        factory.accept(builder);

        element.setProperty(UIProperty.TYPE, elementType);
        this.roots.add(element);
        this.allElements.add(element);
        return element;
    }

    @Override
    public void update(final Mouse mouse) {
        this.allElements.forEach(this.labelSizeResolver::resolve);
        this.roots.forEach(this::updateBounds);

        this.hoverEventProvider.tick(getRoots(), this.eventBus, mouse);
        this.clickEventProvider.tick(getRoots(), this.eventBus, mouse, this.timeManager);
    }

    private void updateBounds(final UIElement element) {
        this.boundaryResolver.resolve(element);
        element.setProperty(UIProperty.CENTER, element.getBounds().getCenter());

        element.getChildren()
               .forEach(this::updateBounds);
    }

    public void addToRoots(final UIElementImpl uiElement) {
        this.roots.add(uiElement);
    }

    public void removeFromRoots(final UIElementImpl uiElement) {
        this.roots.remove(uiElement);
    }

    @SuppressWarnings("unchecked")
    public <T> UIPropertyContainer<T> getPropertyContainer(final UIProperty<T> property) {
        return this.propertyContainers.computeIfAbsent(property,
                                                       key -> new UIPropertyContainer<>(property.name(),
                                                                                        property.defaultValue()));
    }
}
