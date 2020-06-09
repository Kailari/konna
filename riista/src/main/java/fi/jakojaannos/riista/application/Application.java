package fi.jakojaannos.riista.application;

public interface Application extends AutoCloseable {
    @Override
    void close();
}
