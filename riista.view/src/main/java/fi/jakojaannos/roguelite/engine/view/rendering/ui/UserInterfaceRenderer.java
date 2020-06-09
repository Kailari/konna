package fi.jakojaannos.roguelite.engine.view.rendering.ui;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.*;

// TODO: Render UI elements to cached render targets so that they can be cached (only updated/dirty
//  parts should be re-rendered)

public class UserInterfaceRenderer implements AutoCloseable {
    private static final int DEFAULT_BORDER_SIZE = 5;

    private final UIRoot uiRoot;
    private final SpriteBatch spriteBatch;
    private final AssetRegistry<Sprite> spriteRegistry;
    private final TextRenderer textRenderer;
    private final ProgressBarRenderer progressBarRenderer;
    private final Font font;

    public UserInterfaceRenderer(
            final Path assetRoot,
            final AssetRegistry<Sprite> spriteRegistry,
            final TextRenderer textRenderer,
            final AssetRegistry<Font> fontRegistry,
            final RenderingBackend backend,
            final UIRoot uiRoot
    ) {
        this.uiRoot = uiRoot;
        this.spriteBatch = backend.createSpriteBatch(assetRoot, "sprite");
        this.spriteRegistry = spriteRegistry;
        this.textRenderer = textRenderer;
        this.font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");

        this.progressBarRenderer = new ProgressBarRenderer(assetRoot, backend);
    }

    public void render(final UserInterface userInterface) {
        userInterface.getRoots()
                     .forEach(this::renderElement);
    }

    private void renderElement(final UIElement uiElement) {
        if (uiElement.getProperty(UIProperty.HIDDEN).orElse(false)) {
            return;
        }
        final var bounds = uiElement.getBounds();

        uiElement.getProperty(UIProperty.SPRITE)
                 .ifPresent(spriteId -> renderPanelBackground(uiElement, bounds, spriteId));
        uiElement.getProperty(UIProperty.TEXT)
                 .ifPresent(text -> renderTextContent(uiElement, bounds, text));
        uiElement.getProperty(UIProperty.PROGRESS)
                 .ifPresent(percent -> renderProgressBar(uiElement, bounds, percent));

        uiElement.getChildren()
                 .forEach(this::renderElement);
    }

    private void renderProgressBar(
            final UIElement uiElement,
            final ElementBoundaries bounds,
            final double percent
    ) {
        final var x = bounds.minX;
        final var y = bounds.minY;
        final var width = bounds.width;
        final var height = bounds.height;

        final var max = uiElement.getProperty(UIProperty.MAX_PROGRESS)
                                 .orElse(1.0);

        this.progressBarRenderer.render(x, y, width, height, percent, max);
    }

    private void renderTextContent(
            final UIElement uiElement,
            final ElementBoundaries bounds,
            final String text
    ) {
        final var fontSize = getFontSizeFor(uiElement);
        final var x = bounds.minX;
        final var y = bounds.minY;
        uiElement.getProperty(UIProperty.COLOR)
                 .ifPresentOrElse(color -> this.textRenderer.draw(x, y, fontSize, this.font, text,
                                                                  color.r, color.g, color.b),
                                  () -> this.textRenderer.draw(x, y, fontSize, this.font, text));
    }

    private void renderPanelBackground(
            final UIElement uiElement,
            final ElementBoundaries bounds,
            final String spriteId
    ) {
        this.spriteBatch.begin();
        final var sprite = this.spriteRegistry.getByAssetName(spriteId);
        final int borderSize = uiElement.getProperty(UIProperty.BORDER_SIZE)
                                        .orElse(DEFAULT_BORDER_SIZE);

        final var x = bounds.minX;
        final var y = bounds.minY;
        final var width = bounds.width;
        final var height = bounds.height;

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

    @Override
    public void close() throws Exception {
        this.progressBarRenderer.close();
    }

    private int getFontSizeFor(final UIElement uiElement) {
        return uiElement.getProperty(UIProperty.FONT_SIZE)
                        .orElseGet(() -> uiElement.getParent()
                                                  .map(this::getFontSizeFor)
                                                  .orElse(this.uiRoot.getFontSize()));
    }
}
