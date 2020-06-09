package fi.jakojaannos.konna.engine.vulkan.descriptor;

import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.riista.vulkan.rendering.ImageView;

public interface CombinedImageSamplerBinding {
    int binding();

    ImageView imageView();

    TextureSampler sampler();
}
