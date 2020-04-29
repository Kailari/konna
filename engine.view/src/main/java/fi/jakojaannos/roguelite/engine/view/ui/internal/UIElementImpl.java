package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.*;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public class UIElementImpl implements UIElement {
    private final int id;

    private final Collection<UIElement> children = new ArrayList<>();
    private final UserInterfaceImpl userInterface;
    private final ElementBoundaries elementBoundaries;

    private boolean active;

    private long clickTimestamp;
    private boolean pressed;

    @Nullable
    private UIElement parent;

    public boolean isActive() {
        return this.active;
    }

    public void setActive(final boolean state) {
        this.active = state;
    }

    @Override
    public Optional<UIElement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public void setParent(@Nullable final UIElement element) {
        if (element == null) {
            if (this.parent != null) {
                ((UIElementImpl) this.parent).children.remove(this);
            }
            this.parent = null;
            this.userInterface.addToRoots(this);
        } else {
            this.parent = element;
            ((UIElementImpl) this.parent).children.add(this);
            this.userInterface.removeFromRoots(this);
        }
    }

    @Override
    public Collection<UIElement> getChildren() {
        return List.copyOf(this.children);
    }

    @Override
    public ElementBoundaries getBounds() {
        return this.elementBoundaries;
    }

    public boolean isPressed() {
        return this.pressed;
    }

    public UIElementImpl(final int id, final UserInterfaceImpl userInterface) {
        this.id = id;
        this.userInterface = userInterface;
        this.elementBoundaries = new ElementBoundaries();
    }

    public void click(final long timestamp) {
        this.clickTimestamp = timestamp;
        this.pressed = true;
    }

    public void release() {
        this.pressed = false;
    }

    @Override
    public <T> void setProperty(final UIProperty<T> property, final T value) {
        this.userInterface.getPropertyContainer(property)
                          .set(this, value);
    }

    @Override
    public <T> Optional<T> getProperty(final UIProperty<T> property) {
        return this.userInterface.getPropertyContainer(property)
                                 .getFor(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof UIElementImpl uiElement) {
            return this.id == uiElement.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
