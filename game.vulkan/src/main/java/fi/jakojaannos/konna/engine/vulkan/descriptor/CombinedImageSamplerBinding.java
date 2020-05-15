package fi.jakojaannos.konna.engine.vulkan.descriptor;

import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.rendering.ImageView;

public record CombinedImageSamplerBinding(
        int binding,
        ImageView imageView,
        TextureSampler sampler
) {}
