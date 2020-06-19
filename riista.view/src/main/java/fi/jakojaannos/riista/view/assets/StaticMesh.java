package fi.jakojaannos.riista.view.assets;

public interface StaticMesh extends Iterable<Mesh>, AutoCloseable {
    @Override
    void close();
}
