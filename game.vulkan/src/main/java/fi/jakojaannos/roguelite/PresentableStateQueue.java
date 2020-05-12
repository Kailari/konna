package fi.jakojaannos.roguelite;

import java.util.concurrent.Semaphore;

public class PresentableStateQueue {
    private final Semaphore frameAvailable = new Semaphore(0);

    private PresentableState writing;
    private PresentableState reading;
    private PresentableState waiting;

    public PresentableStateQueue() {
        this.writing = new PresentableState();
        this.reading = new PresentableState();
        this.waiting = new PresentableState();
    }

    public PresentableState swapWriting() {
        synchronized (this) {
            final var tmp = this.waiting;
            this.waiting = this.writing;
            this.writing = tmp;
        }

        this.frameAvailable.release();
        return this.writing;
    }

    public PresentableState swapReading() {
        synchronized (this) {
            final var tmp = this.waiting;
            this.waiting = this.reading;
            this.reading = tmp;
        }

        try {
            this.frameAvailable.acquire();
            this.frameAvailable.drainPermits();
        } catch (final InterruptedException ignored) {
        }

        return this.reading;
    }
}
