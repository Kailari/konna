package fi.jakojaannos.riista.vulkan.assets.mesh.staticmesh;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import fi.jakojaannos.riista.view.assets.Mesh;
import fi.jakojaannos.riista.view.assets.StaticMesh;

public class StaticMeshImpl implements StaticMesh {
    private final Collection<Mesh> submeshes;

    public StaticMeshImpl(final Mesh[] submeshes) {
        this.submeshes = List.of(submeshes);
    }

    @Override
    public Iterator<Mesh> iterator() {
        return this.submeshes.iterator();
    }

    @Override
    public void close() {
        this.submeshes.forEach(Mesh::close);
    }
}
