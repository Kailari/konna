package fi.jakojaannos.konna.engine.assets;

import fi.jakojaannos.konna.engine.vulkan.rendering.ImageView;

public interface Texture extends AutoCloseable {
    ImageView getImageView();

    @Override
    void close();
}
