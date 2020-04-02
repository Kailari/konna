package fi.jakojaannos.roguelite.engine.network;

import fi.jakojaannos.roguelite.engine.network.internal.CommandChannelRunnable;

public abstract class NetworkManager<TRunnable extends CommandChannelRunnable>
        implements AutoCloseable {

    private final TRunnable commandChannel;

    public TRunnable getCommandChannel() {
        return this.commandChannel;
    }

    public boolean isConnected() {
        return this.commandChannel.isConnected();
    }

    protected NetworkManager(final TRunnable networkThread) {
        this.commandChannel = networkThread;
        new Thread(this.commandChannel, "Network").start();
    }

    @Override
    public void close() {
        this.commandChannel.close();
    }
}
