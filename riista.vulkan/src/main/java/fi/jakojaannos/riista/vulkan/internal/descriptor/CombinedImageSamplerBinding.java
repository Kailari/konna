package fi.jakojaannos.riista.vulkan.internal.descriptor;

import fi.jakojaannos.riista.vulkan.internal.TextureSampler;
import fi.jakojaannos.riista.vulkan.rendering.ImageView;

public interface CombinedImageSamplerBinding {
    int binding();

    ImageView imageView();

    TextureSampler sampler();
}
