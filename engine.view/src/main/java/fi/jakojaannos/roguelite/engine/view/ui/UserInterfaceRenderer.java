package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.ui.UIElement;
import fi.jakojaannos.roguelite.engine.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class UserInterfaceRenderer {
    private static final int DEFAULT_BORDER_SIZE = 5;
    private static final int ROOT_FONT_SIZE = 12;

    private final SpriteBatch spriteBatch;
    private final AssetRegistry<Sprite> spriteRegistry;
    private final TextRenderer textRenderer;
    private final Font font;

    public UserInterfaceRenderer(
            final SpriteBatch spriteBatch,
            final AssetRegistry<Sprite> spriteRegistry,
            final TextRenderer textRenderer,
            final AssetRegistry<Font> fontRegistry
    ) {
        this.spriteBatch = spriteBatch;
        this.spriteRegistry = spriteRegistry;
        this.textRenderer = textRenderer;
        this.font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");
    }

    public void render(final UserInterface userInterface) {
        userInterface.getRoots()
                     .forEach(this::renderElement);
    }

    private void renderElement(final UIElement uiElement) {
        if (uiElement.getProperty(UIProperty.HIDDEN)
                     .orElse(false)) {
            return;
        }
        uiElement.getProperty(UIProperty.SPRITE)
                 .ifPresent(spriteId -> renderPanelBackground(uiElement, spriteId));
        uiElement.getProperty(UIProperty.TEXT)
                 .ifPresent(text -> renderTextContent(uiElement, text));

        uiElement.getChildren()
                 .forEach(this::renderElement);
    }

    private void renderTextContent(final UIElement uiElement, final String text) {
        val fontSize = getFontSizeFor(uiElement);
        val x = uiElement.getProperty(UIProperty.MIN_X).orElseThrow();
        val y = uiElement.getProperty(UIProperty.MIN_Y).orElseThrow();
        this.textRenderer.drawOnScreen(x, y, fontSize, this.font, text);
    }

    private static int getFontSizeFor(final UIElement uiElement) {
        return uiElement.getProperty(UIProperty.FONT_SIZE)
                        .orElseGet(() -> uiElement.getParent()
                                                  .map(UserInterfaceRenderer::getFontSizeFor)
                                                  .orElse(ROOT_FONT_SIZE));
    }

    private void renderPanelBackground(final UIElement uiElement, final String spriteId) {
        this.spriteBatch.begin();
        val sprite = this.spriteRegistry.getByAssetName(spriteId);
        int borderSize = uiElement.getProperty(UIProperty.BORDER_SIZE)
                                  .orElse(DEFAULT_BORDER_SIZE);

        val x = uiElement.getProperty(UIProperty.MIN_X).orElseThrow();
        val y = uiElement.getProperty(UIProperty.MIN_Y).orElseThrow();
        val width = uiElement.getProperty(UIProperty.WIDTH).orElseThrow();
        val height = uiElement.getProperty(UIProperty.HEIGHT).orElseThrow();

        drawPanelRow(sprite, 0, x, y, width, borderSize, borderSize);
        drawPanelRow(sprite, 1, x, y + borderSize, width, height - 2 * borderSize, borderSize);
        drawPanelRow(sprite, 2, x, y + height - borderSize, width, borderSize, borderSize);
        this.spriteBatch.end();
    }

    private void drawPanelRow(
            final Sprite sprite,
            final int row,
            final double x,
            final double y,
            final double width,
            final double height,
            final double borderSize
    ) {
        this.spriteBatch.draw(sprite, "panel_normal", row * 3, x, y, borderSize, height);
        this.spriteBatch.draw(sprite, "panel_normal", row * 3 + 1, x + borderSize, y, width - 2 * borderSize, height);
        this.spriteBatch.draw(sprite, "panel_normal", row * 3 + 2, x + width - borderSize, y, borderSize, height);
    }
}
