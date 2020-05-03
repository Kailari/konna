package fi.jakojaannos.roguelite.util;

public abstract class RecreateCloseable implements AutoCloseable {
    private boolean cleanedUp = true;

    protected abstract boolean isRecreateRequired();

    protected abstract void recreate();

    protected abstract void cleanup();

    public void tryRecreate() {
        // Always recreate if cleaned up; otherwise, ask the implementation if the recreate is required
        if (!this.cleanedUp && !isRecreateRequired()) {
            return;
        }

        tryCleanup();
        recreate();

        // Recreation done, not cleaned up anymore
        this.cleanedUp = false;
    }

    private void tryCleanup() {
        if (this.cleanedUp) {
            return;
        }

        cleanup();

        this.cleanedUp = true;
    }

    @Override
    public void close() {
        tryCleanup();
    }
}
