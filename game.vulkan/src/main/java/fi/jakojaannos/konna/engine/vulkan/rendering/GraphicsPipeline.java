package fi.jakojaannos.konna.engine.vulkan.rendering;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;

import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.util.shader.ShaderCompiler;
import fi.jakojaannos.konna.engine.vulkan.VertexFormat;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;

import static fi.jakojaannos.konna.engine.util.VkUtil.ensureSuccess;
import static fi.jakojaannos.konna.engine.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class GraphicsPipeline<TVertex> extends RecreateCloseable {
    private static final int ALL_COLOR_COMPONENTS = VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT;

    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final RenderPass renderPass;

    private final ByteBuffer compiledVertexShader;
    private final ByteBuffer compiledFragmentShader;
    private final VertexFormat<TVertex> vertexFormat;
    private final DescriptorSetLayout[] descriptorSetLayouts;

    private long pipelineLayout;
    private long handle;

    public long getHandle() {
        return this.handle;
    }

    @Override
    protected boolean isRecreateRequired() {
        return true;
    }

    public long getLayout() {
        return this.pipelineLayout;
    }

    public GraphicsPipeline(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final RenderPass renderPass,
            final Path vertexShaderPath,
            final Path fragmentShaderPath,
            final VertexFormat<TVertex> vertexFormat,
            final DescriptorSetLayout... descriptorSetLayouts
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.renderPass = renderPass;
        this.vertexFormat = vertexFormat;
        this.descriptorSetLayouts = descriptorSetLayouts;

        try {
            this.compiledVertexShader = ShaderCompiler.loadGLSLShader(vertexShaderPath,
                                                                      VK_SHADER_STAGE_VERTEX_BIT);
            this.compiledFragmentShader = ShaderCompiler.loadGLSLShader(fragmentShaderPath,
                                                                        VK_SHADER_STAGE_FRAGMENT_BIT);
        } catch (final IOException e) {
            throw new IllegalStateException("Loading shaders failed: " + e);
        }
    }

    @Override
    protected void cleanup() {
        vkDestroyPipeline(this.deviceContext.getDevice(), this.handle, null);
        vkDestroyPipelineLayout(this.deviceContext.getDevice(), this.pipelineLayout, null);
    }

    @Override
    protected void recreate() {
        try (final var stack = stackPush();
             final var vertexShaderModule = new ShaderModule(this.deviceContext.getDevice(),
                                                             this.compiledVertexShader);
             final var fragmentShaderModule = new ShaderModule(this.deviceContext.getDevice(),
                                                               this.compiledFragmentShader)
        ) {
            final var shaderStages = createShaderStages(stack, vertexShaderModule, fragmentShaderModule);
            final var vertexInputState = createVertexInputInfo();
            final var inputAssembly = createInputAssembly();
            final var viewportState = createViewportState(this.swapchain.getExtent());
            final var rasterizer = createRasterizer();
            final var multisampling = createMultisamplingStateInfo();
            final var depthStencil = createDepthStencilState();
            final var colorBlendAttachments = createColorBlendAttachment();
            //final var dynamicState = createDynamicState();

            this.pipelineLayout = createPipelineLayout();

            final var createInfo = VkGraphicsPipelineCreateInfo
                    .callocStack(1)
                    .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                    .pStages(shaderStages)
                    .pVertexInputState(vertexInputState)
                    .pInputAssemblyState(inputAssembly)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterizer)
                    .pMultisampleState(multisampling)
                    .pDepthStencilState(depthStencil)
                    .pColorBlendState(colorBlendAttachments)
                    .layout(this.pipelineLayout)
                    .renderPass(this.renderPass.getHandle())
                    .subpass(0);

            final var pPipeline = stack.mallocLong(1);
            final var result = vkCreateGraphicsPipelines(this.deviceContext.getDevice(),
                                                         VK_NULL_HANDLE,
                                                         createInfo,
                                                         null,
                                                         pPipeline);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating graphics pipeline failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pPipeline.get(0);
        }
    }

    private VkPipelineDepthStencilStateCreateInfo createDepthStencilState() {
        return VkPipelineDepthStencilStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
                .depthTestEnable(true)
                .depthWriteEnable(true)
                .depthCompareOp(VK_COMPARE_OP_LESS)
                .depthBoundsTestEnable(false)
                .stencilTestEnable(false);
    }

    private long createPipelineLayout() {
        try (final var stack = stackPush()) {
            final var constantRanges = VkPushConstantRange
                    .callocStack(1)
                    .offset(0)
                    .size(16 * Float.BYTES)
                    .stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            final var createInfo = VkPipelineLayoutCreateInfo
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                    .pPushConstantRanges(constantRanges)
                    .pSetLayouts(stack.longs(Arrays.stream(this.descriptorSetLayouts)
                                                   .mapToLong(DescriptorSetLayout::getHandle)
                                                   .toArray()));

            final var pLayout = stack.mallocLong(1);
            ensureSuccess(vkCreatePipelineLayout(this.deviceContext.getDevice(),
                                                 createInfo,
                                                 null,
                                                 pLayout),
                          "Creating pipeline layout failed");
            return pLayout.get(0);
        }
    }

    private VkPipelineColorBlendStateCreateInfo createColorBlendAttachment() {
        final var attachmentState = VkPipelineColorBlendAttachmentState
                .callocStack(1)
                .colorWriteMask(ALL_COLOR_COMPONENTS)
                .blendEnable(true)
                .srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA)
                .dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
                .colorBlendOp(VK_BLEND_OP_ADD)
                .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
                .dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
                .alphaBlendOp(VK_BLEND_OP_ADD);

        return VkPipelineColorBlendStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                .logicOpEnable(false)
                .pAttachments(attachmentState);
    }

    private VkPipelineVertexInputStateCreateInfo createVertexInputInfo() {
        return VkPipelineVertexInputStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexAttributeDescriptions(this.vertexFormat.getAttributes())
                .pVertexBindingDescriptions(this.vertexFormat.getBindings());
    }

    private VkPipelineInputAssemblyStateCreateInfo createInputAssembly() {
        return VkPipelineInputAssemblyStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                .topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                .primitiveRestartEnable(false);
    }

    private VkPipelineViewportStateCreateInfo createViewportState(final VkExtent2D swapchainExtent) {
        final var viewport = VkViewport
                .callocStack(1)
                .x(0.0f).y(0.0f)
                .width(swapchainExtent.width()).height(swapchainExtent.height())
                .minDepth(0.0f).maxDepth(1.0f);
        final var scissors = VkRect2D
                .callocStack(1)
                .offset(VkOffset2D.callocStack().set(0, 0))
                .extent(swapchainExtent);

        return VkPipelineViewportStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                .viewportCount(1)
                .pViewports(viewport)
                .scissorCount(1)
                .pScissors(scissors);
    }

    private VkPipelineRasterizationStateCreateInfo createRasterizer() {
        return VkPipelineRasterizationStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .depthClampEnable(false)
                .rasterizerDiscardEnable(false)
                .polygonMode(VK_POLYGON_MODE_FILL)
                .lineWidth(1.0f)
                .cullMode(VK_CULL_MODE_BACK_BIT)
                .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
                .depthBiasEnable(false);
    }

    private VkPipelineMultisampleStateCreateInfo createMultisamplingStateInfo() {
        return VkPipelineMultisampleStateCreateInfo
                .callocStack()
                .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .sampleShadingEnable(false)
                .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
    }

    private VkPipelineShaderStageCreateInfo.Buffer createShaderStages(
            final MemoryStack stack,
            final ShaderModule vertexShaderModule,
            final ShaderModule fragmentShaderModule
    ) {
        final var shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2);
        shaderStages.get(0)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_VERTEX_BIT)
                    .module(vertexShaderModule.getHandle())
                    .pName(stack.UTF8("main"));
        shaderStages.get(1)
                    .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                    .stage(VK_SHADER_STAGE_FRAGMENT_BIT)
                    .module(fragmentShaderModule.getHandle())
                    .pName(stack.UTF8("main"));

        return shaderStages;
    }
}
