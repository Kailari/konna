package fi.jakojaannos.roguelite.engine.network;

import fi.jakojaannos.roguelite.engine.network.internal.CommandChannelRunnable;
import lombok.Getter;

public abstract class NetworkManager<TRunnable extends CommandChannelRunnable>
        implements AutoCloseable {

    public boolean isConnected() {
        return this.commandChannel.isConnected();
    }

    @Getter private final TRunnable commandChannel;

    protected NetworkManager(final TRunnable networkThread) {
        this.commandChannel = networkThread;
        new Thread(this.commandChannel, "Network").start();
    }

    @Override
    public void close() {
        this.commandChannel.close();
    }
}
