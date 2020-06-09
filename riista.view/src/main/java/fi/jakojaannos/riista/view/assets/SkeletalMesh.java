package fi.jakojaannos.riista.view.assets;

public interface SkeletalMesh extends Iterable<Mesh>, AutoCloseable {
    @Override
    void close();
}
