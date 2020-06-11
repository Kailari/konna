package fi.jakojaannos.riista.vulkan.internal.types;

import fi.jakojaannos.riista.utilities.BitFlags;

import static org.lwjgl.vulkan.VK10.*;

public enum VkPipelineStageFlagBits implements BitFlags {
    TOP_OF_PIPE_BIT(VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT),
    DRAW_INDIRECT_BIT(VK_PIPELINE_STAGE_DRAW_INDIRECT_BIT),
    VERTEX_INPUT_BIT(VK_PIPELINE_STAGE_VERTEX_INPUT_BIT),
    VERTEX_SHADER_BIT(VK_PIPELINE_STAGE_VERTEX_SHADER_BIT),
    TESSELLATION_CONTROL_SHADER_BIT(VK_PIPELINE_STAGE_TESSELLATION_CONTROL_SHADER_BIT),
    TESSELLATION_EVALUATION_SHADER_BIT(VK_PIPELINE_STAGE_TESSELLATION_EVALUATION_SHADER_BIT),
    GEOMETRY_SHADER_BIT(VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT),
    FRAGMENT_SHADER_BIT(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT),
    EARLY_FRAGMENT_TESTS_BIT(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT),
    LATE_FRAGMENT_TESTS_BIT(VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT),
    COLOR_ATTACHMENT_OUTPUT_BIT(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT),
    COMPUTE_SHADER_BIT(VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT),
    TRANSFER_BIT(VK_PIPELINE_STAGE_TRANSFER_BIT),
    BOTTOM_OF_PIPE_BIT(VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT),
    HOST_BIT(VK_PIPELINE_STAGE_HOST_BIT),
    ALL_GRAPHICS_BIT(VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT),
    ALL_COMMANDS_BIT(VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    VkPipelineStageFlagBits(final int mask) {
        this.mask = mask;
    }
}
