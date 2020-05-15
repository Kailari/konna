package fi.jakojaannos.konna.engine;

import fi.jakojaannos.konna.engine.renderer.DebugRenderer;
import fi.jakojaannos.konna.engine.renderer.RenderBuffer;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class PresentableState {
    private final RenderBuffer<DebugRenderer.TransformEntry> transformEntries = new RenderBuffer<>(DebugRenderer.TransformEntry::new,
                                                                                                   DebugRenderer.TransformEntry[]::new);
    private long timestamp;

    public RenderBuffer<DebugRenderer.TransformEntry> transforms() {
        return this.transformEntries;
    }


    public long timestamp() {
        return this.timestamp;
    }

    public void clear(final TimeManager timeManager) {
        this.transformEntries.reset();
        this.timestamp = timeManager.getCurrentGameTime();
    }
}
