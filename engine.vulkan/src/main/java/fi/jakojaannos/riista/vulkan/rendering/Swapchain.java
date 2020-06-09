package fi.jakojaannos.riista.vulkan.rendering;

import org.lwjgl.vulkan.*;

import fi.jakojaannos.konna.engine.util.RecreateCloseable;
import fi.jakojaannos.konna.engine.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.engine.vulkan.device.SwapchainSupportDetails;
import fi.jakojaannos.konna.engine.vulkan.types.VkFormat;
import fi.jakojaannos.konna.engine.vulkan.types.VkImageAspectFlags;
import fi.jakojaannos.konna.engine.vulkan.window.Window;
import fi.jakojaannos.konna.engine.vulkan.window.WindowSurface;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static fi.jakojaannos.konna.engine.util.VkUtil.translateVulkanResult;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class Swapchain extends RecreateCloseable {
    private final DeviceContext deviceContext;
    private final WindowSurface surface;
    private final VkExtent2D extent;
    private final Window window;

    private long handle;

    private ImageView[] imageViews;
    private VkFormat imageFormat;

    public VkFormat getImageFormat() {
        return this.imageFormat;
    }

    public VkExtent2D getExtent() {
        return this.extent;
    }

    public ImageView[] getImageViews() {
        return this.imageViews;
    }

    public int getImageCount() {
        return this.imageViews.length;
    }

    public long getHandle() {
        return this.handle;
    }

    public Swapchain(
            final DeviceContext deviceContext,
            final Window window,
            final WindowSurface surface
    ) {
        this.deviceContext = deviceContext;
        this.window = window;
        this.surface = surface;
        this.extent = VkExtent2D.calloc();
    }

    @Override
    protected void recreate() {
        final var queueFamilies = this.deviceContext.getQueueFamilies();

        try (final var stack = stackPush()) {
            final var swapChainSupport = SwapchainSupportDetails.query(this.deviceContext.getPhysicalDevice(),
                                                                       this.surface);

            final var surfaceFormat = chooseSurfaceFormat(swapChainSupport.formats());
            final var presentMode = choosePresentMode(swapChainSupport.presentModes());

            final var extent = chooseExtent(this.window, swapChainSupport.capabilities());

            final int imageCount = Math.min(swapChainSupport.capabilities().minImageCount() + 1,
                                            swapChainSupport.capabilities().maxImageCount());

            final var createInfo = VkSwapchainCreateInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(this.surface.getHandle())
                    .minImageCount(imageCount)
                    .imageFormat(surfaceFormat.format())
                    .imageColorSpace(surfaceFormat.colorSpace())
                    .imageExtent(extent)
                    .imageArrayLayers(1)
                    .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                    .preTransform(swapChainSupport.capabilities().currentTransform())
                    .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                    .presentMode(presentMode)
                    .clipped(true)
                    .oldSwapchain(VK_NULL_HANDLE);

            if (queueFamilies.graphicsAndPresentAreSame()) {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
                createInfo.pQueueFamilyIndices(stack.mallocInt(0));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(queueFamilies.graphics(),
                                                          queueFamilies.present()));
            }

            final var pSwapchain = stack.mallocLong(1);
            final var result = vkCreateSwapchainKHR(this.deviceContext.getDevice(),
                                                    createInfo,
                                                    null,
                                                    pSwapchain);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating swapchain failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pSwapchain.get(0);
            this.extent.set(swapChainSupport.capabilities().currentExtent());
            this.imageFormat = VkFormat.valueOf(surfaceFormat.format());

            final long[] images = getSwapchainImages(this.deviceContext.getDevice(), this.handle);
            this.imageViews = new ImageView[images.length];
            for (int i = 0; i < this.imageViews.length; i++) {
                this.imageViews[i] = new ImageView(this.deviceContext,
                                                   images[i],
                                                   this.imageFormat,
                                                   bitMask(VkImageAspectFlags.COLOR_BIT));
            }
        }
    }

    @Override
    protected void cleanup() {
        for (final var imageView : this.imageViews) {
            imageView.close();
        }
        vkDestroySwapchainKHR(this.deviceContext.getDevice(), this.handle, null);
    }

    @Override
    public void close() {
        super.close();
        this.extent.free();
    }

    private static long[] getSwapchainImages(final VkDevice device, final long handle) {
        try (final var stack = stackPush()) {
            final var pCount = stack.mallocInt(1);
            final var countResult = vkGetSwapchainImagesKHR(device, handle, pCount, null);
            if (countResult != VK_SUCCESS) {
                throw new IllegalStateException("Getting swapchain image count failed: "
                                                + translateVulkanResult(countResult));
            }

            final var pImages = stack.mallocLong(pCount.get(0));
            final var queryResult = vkGetSwapchainImagesKHR(device, handle, pCount, pImages);
            if (queryResult != VK_SUCCESS) {
                throw new IllegalStateException("Getting swapchain images failed: "
                                                + translateVulkanResult(countResult));
            }

            final var images = new long[pCount.get()];
            pImages.get(images);
            return images;
        }
    }

    private static VkExtent2D chooseExtent(final Window window, final VkSurfaceCapabilitiesKHR capabilities) {
        try (final var stack = stackPush()) {
            // -1L == UINT32_MAX
            if (capabilities.currentExtent().width() != -1L && capabilities.currentExtent().height() != -1L) {
                return capabilities.currentExtent();
            } else {
                final var pWidth = stack.mallocInt(1);
                final var pHeight = stack.mallocInt(1);
                glfwGetFramebufferSize(window.getHandle(), pWidth, pHeight);

                final var windowWidth = pWidth.get(0);
                final var windowHeight = pHeight.get(0);

                final var actualExtent = VkExtent2D
                        .callocStack()
                        .set(windowWidth, windowHeight);

                final var minExtent = capabilities.minImageExtent();
                final var maxExtent = capabilities.maxImageExtent();
                actualExtent.width(Math.max(minExtent.width(),
                                            Math.min(maxExtent.width(), actualExtent.width())));
                actualExtent.height(Math.max(minExtent.height(),
                                             Math.min(maxExtent.height(), actualExtent.height())));

                return actualExtent;
            }
        }
    }

    private static int choosePresentMode(final int[] presentModes) {
        /* Uncomment for unlimited FPS
        for (final var presentMode : presentModes) {
            if (presentMode == VK_PRESENT_MODE_MAILBOX_KHR) {
                return presentMode;
            }
        }
        */
        return VK_PRESENT_MODE_FIFO_KHR;
    }

    private static VkSurfaceFormatKHR chooseSurfaceFormat(final VkSurfaceFormatKHR[] availableFormats) {
        for (final var availableFormat : availableFormats) {
            if (hasDesiredFormat(availableFormat) && hasDesiredColorSpace(availableFormat)) {
                return availableFormat;
            }
        }

        return availableFormats[0];
    }

    private static boolean hasDesiredFormat(final VkSurfaceFormatKHR format) {
        return format.format() == VK_FORMAT_B8G8R8A8_SRGB;
    }

    private static boolean hasDesiredColorSpace(final VkSurfaceFormatKHR format) {
        return format.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
    }
}
