package fi.jakojaannos.riista.vulkan.util;

public abstract class RecreateCloseable implements AutoCloseable {
    private boolean cleanedUp = true;
    private long generation;

    protected boolean isRecreateRequired() {
        return true;
    }

    protected boolean isOlderThan(final RecreateCloseable other) {
        return this.generation < other.generation;
    }

    protected abstract void recreate();

    protected abstract void cleanup();

    public final void tryRecreate() {
        // Always recreate if cleaned up; otherwise, ask the implementation if the recreate is required
        if (!this.cleanedUp && !isRecreateRequired()) {
            return;
        }

        tryCleanup();
        recreate();
        ++this.generation;

        // Recreation done, not cleaned up anymore
        this.cleanedUp = false;
    }

    @Override
    public void close() {
        tryCleanup();
    }

    private void tryCleanup() {
        if (this.cleanedUp) {
            return;
        }

        cleanup();

        this.cleanedUp = true;
    }

    public boolean isCleanedUp() {
        return this.cleanedUp;
    }
}
