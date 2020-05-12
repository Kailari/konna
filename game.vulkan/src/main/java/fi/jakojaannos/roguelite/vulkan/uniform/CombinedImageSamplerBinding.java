package fi.jakojaannos.roguelite.vulkan.uniform;

import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.rendering.ImageView;

public record CombinedImageSamplerBinding(
        int binding,
        ImageView imageView,
        TextureSampler sampler
) {}
