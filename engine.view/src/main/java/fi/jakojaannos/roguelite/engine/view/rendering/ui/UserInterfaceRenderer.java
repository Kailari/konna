package fi.jakojaannos.roguelite.engine.view.rendering.ui;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

// TODO: Render UI elements to cached render targets so that they can be cached (only updated/dirty
//  parts should be re-rendered)

@Slf4j
public class UserInterfaceRenderer implements AutoCloseable {
    private static final int DEFAULT_BORDER_SIZE = 5;
    private static final int ROOT_FONT_SIZE = 12;

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
            final RenderingBackend backend
    ) {
        this.spriteBatch = backend.createSpriteBatch(assetRoot, "sprite");
        this.spriteRegistry = spriteRegistry;
        this.textRenderer = textRenderer;
        this.font = fontRegistry.getByAssetName("fonts/VCR_OSD_MONO.ttf");

        this.progressBarRenderer = new ProgressBarRenderer(assetRoot, backend);
    }

    private static int getFontSizeFor(final UIElement uiElement) {
        return uiElement.getProperty(UIProperty.FONT_SIZE)
                        .orElseGet(() -> uiElement.getParent()
                                                  .map(UserInterfaceRenderer::getFontSizeFor)
                                                  .orElse(ROOT_FONT_SIZE));
    }

    public void render(final UserInterface userInterface) {
        userInterface.getRoots()
                     .forEach(this::renderElement);
    }

    private void renderElement(final UIElement uiElement) {
        if (uiElement.getProperty(UIProperty.HIDDEN).orElse(false)) {
            return;
        }
        uiElement.getProperty(UIProperty.SPRITE)
                 .ifPresent(spriteId -> renderPanelBackground(uiElement, spriteId));
        uiElement.getProperty(UIProperty.TEXT)
                 .ifPresent(text -> renderTextContent(uiElement, text));
        uiElement.getProperty(UIProperty.PROGRESS)
                 .ifPresent(percent -> renderProgressBar(uiElement, percent));

        uiElement.getChildren()
                 .forEach(this::renderElement);
    }

    private void renderProgressBar(final UIElement uiElement, final double percent) {
        final var x = uiElement.getProperty(UIProperty.MIN_X).orElseThrow();
        final var y = uiElement.getProperty(UIProperty.MIN_Y).orElseThrow();
        final var width = uiElement.getProperty(UIProperty.WIDTH).orElseThrow();
        final var height = uiElement.getProperty(UIProperty.HEIGHT).orElseThrow();

        final var max = uiElement.getProperty(UIProperty.MAX_PROGRESS)
                                 .orElse(1.0);

        this.progressBarRenderer.render(x, y, width, height, percent, max);
    }

    private void renderTextContent(final UIElement uiElement, final String text) {
        final var fontSize = getFontSizeFor(uiElement);
        final var x = uiElement.getProperty(UIProperty.MIN_X).orElseThrow();
        final var y = uiElement.getProperty(UIProperty.MIN_Y).orElseThrow();
        this.textRenderer.draw(x, y, fontSize, this.font, text);
    }

    private void renderPanelBackground(final UIElement uiElement, final String spriteId) {
        this.spriteBatch.begin();
        final var sprite = this.spriteRegistry.getByAssetName(spriteId);
        int borderSize = uiElement.getProperty(UIProperty.BORDER_SIZE)
                                  .orElse(DEFAULT_BORDER_SIZE);

        final var x = uiElement.getProperty(UIProperty.MIN_X).orElseThrow();
        final var y = uiElement.getProperty(UIProperty.MIN_Y).orElseThrow();
        final var width = uiElement.getProperty(UIProperty.WIDTH).orElseThrow();
        final var height = uiElement.getProperty(UIProperty.HEIGHT).orElseThrow();

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
}
