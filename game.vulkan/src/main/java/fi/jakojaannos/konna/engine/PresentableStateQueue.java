package fi.jakojaannos.konna.engine;

public class PresentableStateQueue {
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

        return this.writing;
    }

    public PresentableState swapReading() {
        synchronized (this) {
            // Do not swap if the waiting frame has not been updated yet
            if (this.reading.timestamp() > this.waiting.timestamp()) {
                return this.reading;
            }

            final var tmp = this.waiting;
            this.waiting = this.reading;
            this.reading = tmp;
        }

        return this.reading;
    }
}
