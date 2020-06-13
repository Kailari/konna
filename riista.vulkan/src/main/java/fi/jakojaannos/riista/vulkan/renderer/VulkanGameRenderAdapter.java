package fi.jakojaannos.riista.vulkan.renderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.GameModeRenderers;
import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.application.PresentableStateQueue;
import fi.jakojaannos.riista.vulkan.application.VulkanApplication;
import fi.jakojaannos.riista.vulkan.renderer.game.RendererRecorder;
import fi.jakojaannos.riista.vulkan.rendering.Swapchain;
import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;

public class VulkanGameRenderAdapter implements GameRenderAdapter<PresentableState> {
    private static final Logger LOG = LoggerFactory.getLogger(VulkanGameRenderAdapter.class);

    private final PresentableStateQueue presentableStateQueue = new PresentableStateQueue();
    private final RendererRecorder renderRecorder = new RendererRecorder();

    private final GameModeRenderers gameModeRenderers;
    private final Swapchain swapchain;

    @Nullable
    private SystemDispatcher renderDispatcher;

    public VulkanGameRenderAdapter(
            final GameModeRenderers gameModeRenderers,
            final VulkanApplication application
    ) {
        this.gameModeRenderers = gameModeRenderers;
        this.swapchain = application.backend().swapchain();
    }

    @Override
    public void onGameModeChange(final GameMode gameMode, final GameState gameState) {
        if (this.renderDispatcher != null) {
            this.renderDispatcher.close();
        }

        final var maybeRenderDispatcher = this.gameModeRenderers.get(gameMode.id());
        maybeRenderDispatcher.ifPresent(dispatcher -> {
            gameState.systems().resetToDefaultState(dispatcher.getSystems());
            gameState.systems().resetGroupsToDefaultState(dispatcher.getGroups());
        });

        this.renderDispatcher = maybeRenderDispatcher.orElse(null);
    }

    @Override
    public void writePresentableState(
            final GameState gameState,
            final Collection<Object> events
    ) {
        if (this.renderDispatcher != null) {
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

            this.renderDispatcher.tick(gameState.world(), gameState.systems(), events);
        }
    }

    @Override
    public PresentableState fetchPresentableState() {
        return this.presentableStateQueue.swapReading();
    }

    @Override
    public void close() {
        if (this.renderDispatcher != null) {
            try {
                LOG.debug("Render dispatcher closing");
                this.renderDispatcher.close();
            } catch (final Throwable t) {
                LOG.error("Disposing render dispatcher failed", t);
            }
        } else {
            LOG.warn("No render dispatcher present.");
        }
    }
}
