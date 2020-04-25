package fi.jakojaannos.roguelite;

import java.nio.file.Path;

public class Main {
    public static void main(final String[] args) {
        try (final var app = Application.initialize(800,
                                                    600,
                                                    Path.of("../assets"))) {
            app.window().show();

            final var renderCommandBuffers = new RenderCommandBuffers(app.graphicsCommandPool(),
                                                                      app.swapchain().getImageCount(),
                                                                      app.renderPass(),
                                                                      app.framebuffers(),
                                                                      app.graphicsPipeline());

            while (app.window().isOpen()) {
                app.window().handleOSEvents();

                drawFrame(renderCommandBuffers);
            }
        }
    }

    private static void drawFrame(final RenderCommandBuffers renderCommandBuffers) {
        // 1. acquire swapchain image
        // 2. submit
    }
}
