package fi.jakojaannos.roguelite.engine.view.content;

import fi.jakojaannos.roguelite.engine.content.AbstractAssetRegistry;
import fi.jakojaannos.roguelite.engine.content.AssetHandle;
import fi.jakojaannos.roguelite.engine.view.rendering.text.Font;
import lombok.val;

import java.nio.file.Path;
import java.util.Optional;

public class FontRegistry extends AbstractAssetRegistry<Font> {
    private final Path assetRoot;
    private final FontLoader fontLoader;
    private final Font defaultFont;

    public FontRegistry(
            final Path assetRoot,
            final FontLoader fontLoader
    ) {
        this.assetRoot = assetRoot;
        this.fontLoader = fontLoader;

        this.defaultFont = loadAsset(new AssetHandle("fonts/VCR_OSD_MONO.ttf")).orElseThrow();
    }

    @Override
    protected Font getDefault() {
        return this.defaultFont;
    }

    @Override
    protected Optional<Font> loadAsset(final AssetHandle handle) {
        val path = this.assetRoot.resolve(handle.getName());
        return Optional.of(this.fontLoader.load(path, 1.0f, 1.0f));
    }

    @Override
    public void close() throws Exception {
        this.forEach((assetHandle, font) -> { try { font.close();} catch (Exception ignored) { } });
        super.close();
    }

    public interface FontLoader {
        Font load(Path filePath, float contentScaleX, float contentScaleY);
    }
}
