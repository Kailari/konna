package fi.jakojaannos.riista.vulkan.internal.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.riista.vulkan.internal.device.DeviceContext;

import static org.lwjgl.vulkan.EXTFilterCubic.VK_FILTER_CUBIC_EXT;
import static org.lwjgl.vulkan.IMGFilterCubic.VK_FILTER_CUBIC_IMG;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;

public enum VkFilter {
    NEAREST(VK_FILTER_NEAREST),
    LINEAR(VK_FILTER_LINEAR),
    CUBIC_IMG(VK_FILTER_CUBIC_IMG) {
        @Override
        public boolean isSupported(final DeviceContext deviceContext) {
            return deviceContext.getDeviceCapabilities().VK_IMG_filter_cubic;
        }
    },
    CUBIC_EXT(VK_FILTER_CUBIC_EXT) {
        @Override
        public boolean isSupported(final DeviceContext deviceContext) {
            return deviceContext.getDeviceCapabilities().VK_EXT_filter_cubic;
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(VkFilter.class);

    private final int filter;

    VkFilter(final int filter) {
        this.filter = filter;
    }

    public int asInt() {
        return this.filter;
    }

    public int safeAsInt(final DeviceContext deviceContext) {
        if (!isSupported(deviceContext)) {
            LOG.error("Image filtering {} not supported! Falling back to LINEAR", this);
            return LINEAR.asInt();
        }

        return this.asInt();
    }

    public boolean isSupported(final DeviceContext deviceContext) {
        return true;
    }
}
