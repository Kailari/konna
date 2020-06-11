package fi.jakojaannos.riista.vulkan.renderer;

import javax.annotation.Nullable;

import fi.jakojaannos.riista.vulkan.rendering.Attachment;

public record RenderSubpass(
        Attachment[]colorAttachments,
        boolean hasDepthAttachment
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable private Attachment[] colorAttachments;
        private boolean hasDepthAttachment;

        public Builder colorAttachments(final Attachment... attachments) {
            this.colorAttachments = attachments;
            return this;
        }

        public Builder withDepthAttachment() {
            this.hasDepthAttachment = true;
            return this;
        }

        public RenderSubpass build() {
            return new RenderSubpass(this.colorAttachments != null
                                             ? this.colorAttachments
                                             : new Attachment[0],
                                     this.hasDepthAttachment);
        }
    }
}
