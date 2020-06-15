package fi.jakojaannos.riista.vulkan.renderer;

import java.util.Collection;
import java.util.function.Consumer;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.GameModeRenderers;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.application.PresentableStateQueue;
import fi.jakojaannos.riista.vulkan.application.VulkanApplication;
import fi.jakojaannos.riista.vulkan.renderer.game.RendererRecorder;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.engine.GameState;

public class VulkanGameRenderAdapter extends GameRenderAdapterBase<PresentableState> {
    private final PresentableStateQueue presentableStateQueue = new PresentableStateQueue();
    private final RendererRecorder renderRecorder = new RendererRecorder();

    private final Swapchain swapchain;

    public VulkanGameRenderAdapter(
            final GameModeRenderers gameModeRenderers,
            final VulkanApplication application,
            final Consumer<CameraProperties> cameraPropertiesUpdater
    ) {
        super(gameModeRenderers, cameraPropertiesUpdater);
        this.swapchain = application.backend().swapchain();
    }

    @Override
    public void writePresentableState(
            final GameState gameState,
            final Collection<Object> events
    ) {
        if (this.hasActiveRenderDispatcher()) {
            final var cameraProperties = gameState.world().fetchResource(CameraProperties.class);
            final var mouse = gameState.world().fetchResource(Mouse.class);
            final var timeManager = gameState.world().fetchResource(TimeManager.class);

            // FIXME: Clear the buffers BEFORE ticking the simulation, update only camera etc. post-tick
            //  - still avoids camera lag
            //  - allows debug rendering from regular systems (!!)
            final var presentableState = this.presentableStateQueue.swapWriting();
            presentableState.clear(timeManager,
                                   mouse.position,
                                   mouse.clicked,
                                   this.swapchain.getExtent(),
                                   cameraProperties.getPosition(),
                                   cameraProperties.getViewMatrix());

            gameState.world().replaceResource(Renderer.class, this.renderRecorder);
            this.renderRecorder.setWriteState(presentableState);

            super.writePresentableState(gameState, events);
        }
    }

    @Override
    public PresentableState fetchPresentableState() {
        return this.presentableStateQueue.swapReading();
    }
}
