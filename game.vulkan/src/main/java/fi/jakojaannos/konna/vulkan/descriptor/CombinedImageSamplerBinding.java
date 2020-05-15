package fi.jakojaannos.konna.vulkan.descriptor;

import fi.jakojaannos.konna.vulkan.TextureSampler;
import fi.jakojaannos.konna.vulkan.rendering.ImageView;

public record CombinedImageSamplerBinding(
        int binding,
        ImageView imageView,
        TextureSampler sampler
) {}
