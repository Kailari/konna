package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public class UIElementLabelSizeResolver {
    private final TextSizeProvider font;
    private final UIRoot uiRoot;

    // FIXME: Read font from UI
    public UIElementLabelSizeResolver(
            final TextSizeProvider font,
            final UIRoot uiRoot
    ) {
        this.font = font;
        this.uiRoot = uiRoot;
    }

    public void resolve(final UIElement uiElement) {
        final var maybeText = uiElement.getProperty(UIProperty.TEXT);
        if (maybeText.isEmpty()) {
            return;
        }

        final var text = maybeText.get();

        final var fontSize = getFontSize(uiElement);
        final var font = this.font; // TODO: Get font from hierarchy like the font size is get

        final int width = (int) font.getStringWidthInPixels(fontSize, text);
        uiElement.setProperty(UIProperty.WIDTH, ProportionValue.absolute(width));

        final int height = (int) font.getStringHeightInPixels(fontSize, text);
        uiElement.setProperty(UIProperty.HEIGHT, ProportionValue.absolute(height));
    }

    private int getFontSize(final UIElement uiElement) {
        return uiElement.getProperty(UIProperty.FONT_SIZE)
                        .orElseGet(() -> uiElement.getParent()
                                                  .map(this::getFontSize)
                                                  .orElseGet(this.uiRoot::getFontSize));
    }
}
