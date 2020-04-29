package fi.jakojaannos.roguelite.vulkan.rendering;

import org.lwjgl.vulkan.*;

import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.device.SwapchainSupportDetails;
import fi.jakojaannos.roguelite.vulkan.window.WindowSurface;

import static fi.jakojaannos.roguelite.util.VkUtil.translateVulkanResult;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class Swapchain implements AutoCloseable {
    private final VkDevice device;
    private final long handle;

    private final VkExtent2D extent;

    private final long[] images;
    private final ImageView[] imageViews;
    private final int imageFormat;

    public int getImageFormat() {
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
            final WindowSurface surface,
            final int windowWidth,
            final int windowHeight
    ) {
        this.device = deviceContext.getDevice();
        final var queueFamilies = deviceContext.getQueueFamilies();

        try (final var stack = stackPush()) {
            final var swapChainSupport = SwapchainSupportDetails.query(deviceContext.getPhysicalDevice(),
                                                                       surface);

            final var surfaceFormat = chooseSurfaceFormat(swapChainSupport.formats());
            final var presentMode = choosePresentMode(swapChainSupport.presentModes());
            final var extent = chooseExtent(swapChainSupport.capabilities(), windowWidth, windowHeight);

            final int imageCount = Math.min(swapChainSupport.capabilities().minImageCount() + 1,
                                            swapChainSupport.capabilities().maxImageCount());

            final var createInfo = VkSwapchainCreateInfoKHR
                    .callocStack()
                    .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                    .surface(surface.getHandle())
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
            final var result = vkCreateSwapchainKHR(this.device, createInfo, null, pSwapchain);
            if (result != VK_SUCCESS) {
                throw new IllegalStateException("Creating swapchain failed: "
                                                + translateVulkanResult(result));
            }
            this.handle = pSwapchain.get(0);
            this.extent = VkExtent2D.calloc().set(swapChainSupport.capabilities().currentExtent());
            this.imageFormat = surfaceFormat.format();

            this.images = getSwapchainImages(this.device, this.handle);
            this.imageViews = new ImageView[this.images.length];
            for (int i = 0; i < this.imageViews.length; i++) {
                this.imageViews[i] = new ImageView(deviceContext, this.images[i], this.imageFormat);
            }
        }
    }

    @Override
    public void close() {
        for (final var imageView : this.imageViews) {
            imageView.close();
        }
        this.extent.free();
        vkDestroySwapchainKHR(this.device, this.handle, null);
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

    private static VkExtent2D chooseExtent(
            final VkSurfaceCapabilitiesKHR capabilities,
            final int windowWidth,
            final int windowHeight
    ) {
        try (final var ignored = stackPush()) {
            // -1L == UINT32_MAX
            if (capabilities.currentExtent().width() != -1L) {
                return capabilities.currentExtent();
            } else {
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
        for (final var presentMode : presentModes) {
            if (presentMode == VK_PRESENT_MODE_MAILBOX_KHR) {
                return presentMode;
            }
        }

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
