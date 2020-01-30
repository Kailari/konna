package fi.jakojaannos.roguelite.engine.network;

import lombok.Getter;

import fi.jakojaannos.roguelite.engine.network.internal.CommandChannelRunnable;

public abstract class NetworkManager<TRunnable extends CommandChannelRunnable>
        implements AutoCloseable {

    @Getter private final TRunnable commandChannel;

    protected NetworkManager(final TRunnable networkThread) {
        this.commandChannel = networkThread;
        new Thread(this.commandChannel, "Network").start();
    }

    public boolean isConnected() {
        return this.commandChannel.isConnected();
    }

    @Override
    public void close() {
        this.commandChannel.close();
    }
}
