package fi.jakojaannos.konna.engine.assets;

import fi.jakojaannos.konna.engine.assets.mesh.staticmesh.StaticMeshImpl;

public interface StaticMesh extends Iterable<Mesh>, AutoCloseable {
    static StaticMesh from(final Mesh... submeshes) {
        return new StaticMeshImpl(submeshes);
    }

    @Override
    void close();
}
