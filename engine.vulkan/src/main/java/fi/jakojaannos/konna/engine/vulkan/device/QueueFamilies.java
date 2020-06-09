package fi.jakojaannos.konna.engine.vulkan.device;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public record QueueFamilies(
        int graphics,
        int transfer,
        int present
) {
    public Collection<Integer> getUniqueIndices() {
        return new HashSet<>(List.of(this.graphics, this.transfer, this.present));
    }

    public boolean isIncomplete() {
        return this.graphics == -1 || this.transfer == -1 || this.present == -1;
    }

    public boolean hasSeparateGraphicsQueue() {
        return this.graphics != this.present
               && this.graphics != this.transfer;
    }

    public boolean hasSeparateTransferQueue() {
        return this.transfer != this.graphics
               && this.transfer != this.present;
    }

    public boolean hasSeparatePresentQueue() {
        return this.present != this.graphics
               && this.present != this.transfer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean graphicsAndPresentAreSame() {
        return this.present == this.graphics;
    }

    public static class Builder {
        private int graphics = -1;
        private int transfer = -1;
        private int present = -1;

        public QueueFamilies build() {
            return new QueueFamilies(this.graphics, this.transfer, this.present);
        }

        public void graphics(final int graphics) {
            this.graphics = graphics;
        }

        public void transfer(final int transfer) {
            this.transfer = transfer;
        }

        public void present(final int present) {
            this.present = present;
        }
    }
}
