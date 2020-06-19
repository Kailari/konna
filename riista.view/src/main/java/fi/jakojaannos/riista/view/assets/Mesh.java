package fi.jakojaannos.riista.view.assets;

public interface Mesh extends AutoCloseable {
    int getIndexCount();

    Material getMaterial();

    @Override
    void close();
}
