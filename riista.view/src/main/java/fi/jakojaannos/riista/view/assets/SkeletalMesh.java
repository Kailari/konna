package fi.jakojaannos.riista.view.assets;

public interface SkeletalMesh extends Iterable<Mesh>, AutoCloseable {
    Animation getAnimation(String name);

    @Override
    void close();
}
