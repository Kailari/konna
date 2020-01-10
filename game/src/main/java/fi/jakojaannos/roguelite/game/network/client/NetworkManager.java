package fi.jakojaannos.roguelite.game.network.client;

public interface NetworkManager extends AutoCloseable {
    boolean isConnected();

    void handleMessageTasksAndFlush();

    @Override
    void close();
}
