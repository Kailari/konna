package fi.jakojaannos.konna.engine.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.Material;
import fi.jakojaannos.konna.engine.assets.Mesh;
import fi.jakojaannos.konna.engine.assets.StaticMesh;
import fi.jakojaannos.konna.engine.assets.material.MaterialImpl;
import fi.jakojaannos.konna.engine.util.BitMask;
import fi.jakojaannos.konna.engine.vulkan.LogCategories;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;

import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiImportFile;

public class StaticMeshLoader extends MeshLoader<StaticMesh> {
    private static final Logger LOG = LoggerFactory.getLogger(StaticMeshLoader.class);

    public StaticMeshLoader(final RenderingBackend backend, final AssetManager assetManager) {
        super(backend, assetManager);
    }

    @Override
    public Optional<StaticMesh> load(
            final Path path,
            final BitMask<AssimpProcess> flags
    ) {
        LOG.debug(LogCategories.MESH_LOADING, "Importing static mesh \"{}\"",
                  path);
        final var scene = aiImportFile(this.assetManager.getRootPath()
                                                        .resolve(path)
                                                        .toString(),
                                       flags.mask());
        if (scene == null) {
            throw new IllegalStateException("Error importing model: " + aiGetErrorString());
        }

        final var numMaterials = scene.mNumMaterials();
        final var materials = scene.mMaterials();

        final var processedMaterials = new ArrayList<MaterialImpl>();
        if (materials != null) {
            for (int i = 0; i < numMaterials; i++) {
                final var aiMaterial = AIMaterial.create(materials.get(i));
                processMaterial(aiMaterial, processedMaterials);
            }
        }

        LOG.debug(LogCategories.MESH_LOADING, "\t-> Loaded {} materials.",
                  processedMaterials.size());

        final var numMeshes = scene.mNumMeshes();
        final var meshes = scene.mMeshes();

        final var processedMeshes = new Mesh[numMeshes];
        if (meshes != null) {
            for (int i = 0; i < numMeshes; i++) {
                final var mesh = AIMesh.create(meshes.get(i));
                final var processedMesh = processMesh(mesh, processedMaterials);
                processedMeshes[i] = processedMesh;
            }
        }

        LOG.debug(LogCategories.MESH_LOADING, "\t-> Loaded {} submeshes.",
                  processedMaterials.size());

        return Optional.of(StaticMesh.from(processedMeshes));
    }

    private Mesh processMesh(
            final AIMesh mesh,
            final ArrayList<MaterialImpl> materials
    ) {
        final var vertices = processVertices(mesh);
        final var indices = processIndices(mesh);

        final Material material;
        final int materialIdx = mesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = this.defaultMaterial;
        }
        return Mesh.from(this.backend,
                         StaticMeshVertex.FORMAT,
                         vertices,
                         indices,
                         material);
    }

    private StaticMeshVertex[] processVertices(final AIMesh mesh) {
        final var vertices = new StaticMeshVertex[mesh.mNumVertices()];

        final var vertexBuffer = mesh.mVertices();
        final var normalBuffer = mesh.mNormals();

        // NOTE: There may be multiple UV sets, load just the first one
        final var uvBuffer = mesh.mTextureCoords(0);
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            final var position = vertexBuffer.get(i);
            final var normal = normalBuffer.get(i);
            final var textureCoordinates = uvBuffer.get(i);

            vertices[i] = new StaticMeshVertex(new Vector3f(position.x(),
                                                            position.y(),
                                                            position.z()),
                                               new Vector3f(normal.x(),
                                                            normal.y(),
                                                            normal.z()),
                                               new Vector2f(textureCoordinates.x(),
                                                            textureCoordinates.y()));
        }

        return vertices;
    }
}
