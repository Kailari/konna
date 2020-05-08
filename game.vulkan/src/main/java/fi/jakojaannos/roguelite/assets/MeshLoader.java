package fi.jakojaannos.roguelite.assets;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.util.BitMask;
import fi.jakojaannos.roguelite.vulkan.LogCategories;
import fi.jakojaannos.roguelite.vulkan.TextureSampler;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.roguelite.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.roguelite.vulkan.device.DeviceContext;
import fi.jakojaannos.roguelite.vulkan.rendering.Swapchain;

import static fi.jakojaannos.roguelite.util.BitMask.bitMask;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class MeshLoader implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(MeshLoader.class);

    private static final Material DEFAULT_MATERIAL = new Material(Material.DEFAULT_COLOR,
                                                                  Material.DEFAULT_COLOR,
                                                                  Material.DEFAULT_COLOR,
                                                                  null,
                                                                  false,
                                                                  0.0f);
    private final DeviceContext deviceContext;
    private final Swapchain swapchain;
    private final DescriptorPool descriptorPool;
    private final DescriptorSetLayout materialLayout;
    private final TextureSampler sampler;

    private final Path assetRoot;
    private final Texture defaultTexture;

    public MeshLoader(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout materialLayout,
            final TextureSampler sampler,
            final Path assetRoot
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;
        this.materialLayout = materialLayout;
        this.sampler = sampler;

        this.assetRoot = assetRoot;

        this.defaultTexture = new Texture(deviceContext,
                                          assetRoot.resolve("textures/vulkan/texture.jpg"));
    }

    public Mesh[] load(final Path path) {
        return load(path,
                    bitMask(AssimpProcess.JoinIdenticalVertices,
                            AssimpProcess.Triangulate));
    }

    public Mesh[] load(
            final Path path,
            final BitMask<AssimpProcess> flags
    ) {
        LOG.debug(LogCategories.MESH_LOADING, "Importing model \"{}\"",
                  path);
        final var scene = aiImportFile(this.assetRoot.resolve(path)
                                                     .toString(),
                                       flags.mask());
        if (scene == null) {
            throw new IllegalStateException("Error importing model: " + aiGetErrorString());
        }

        final var numMaterials = scene.mNumMaterials();
        final var materials = scene.mMaterials();
        final var processedMaterials = new ArrayList<Material>();
        for (int i = 0; i < numMaterials; i++) {
            final var aiMaterial = AIMaterial.create(materials.get(i));
            processMaterial(aiMaterial, processedMaterials);
        }

        LOG.debug(LogCategories.MESH_LOADING, "\t-> Loaded {} materials.",
                  processedMaterials.size());

        final var numMeshes = scene.mNumMeshes();
        final var meshes = scene.mMeshes();
        final var processedMeshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            final var mesh = AIMesh.create(meshes.get(i));
            final var processedMesh = processMesh(mesh, processedMaterials);
            processedMeshes[i] = processedMesh;
        }

        LOG.debug(LogCategories.MESH_LOADING, "\t-> Loaded {} submeshes.",
                  processedMaterials.size());

        return processedMeshes;
    }

    private void processMaterial(
            final AIMaterial material,
            final ArrayList<Material> processedMaterials
    ) {
        LOG.trace(LogCategories.MESH_LOADING, "\tProcessing material");
        try (final var ignored = stackPush()) {
            final var color = AIColor4D.callocStack();

            final var path = AIString.callocStack();
            final Texture texture;
            final boolean hasTexture;
            if (aiGetMaterialTexture(material,
                                     aiTextureType_DIFFUSE,
                                     0,
                                     path,
                                     (IntBuffer) null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null) == aiReturn_SUCCESS
            ) {
                hasTexture = true;
                texture = tryGetTexture(path.dataString());
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> Got texture: {}",
                          path.dataString());
            } else {
                hasTexture = false;
                texture = this.defaultTexture;
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> No texture.");
            }

            // Try fetching the ambient color
            final Vector4f ambient;
            if (aiGetMaterialColor(material, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> Got ambient: {}",
                          ambient);
            } else {
                ambient = Material.DEFAULT_COLOR;
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> No ambient.");
            }

            // Try fetching the diffuse color
            final Vector4f diffuse;
            if (aiGetMaterialColor(material, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> Got diffuse: {}",
                          diffuse);
            } else {
                diffuse = Material.DEFAULT_COLOR;
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> No diffuse.");
            }

            // Try fetching the diffuse color
            final Vector4f specular;
            if (aiGetMaterialColor(material, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                specular = new Vector4f(color.r(), color.g(), color.b(), color.a());
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> Got specular: {}",
                          specular);
            } else {
                specular = Material.DEFAULT_COLOR;
                LOG.trace(LogCategories.MESH_LOADING, "\t\t-> No specular.");
            }

            final var processed = new Material(ambient,
                                               diffuse,
                                               specular,
                                               texture,
                                               hasTexture,
                                               1.0f);
            processedMaterials.add(processed);
        }
    }

    private Mesh processMesh(final AIMesh mesh, final ArrayList<Material> materials) {
        final var vertices = new MeshVertex[mesh.mNumVertices()];

        final var vertexBuffer = mesh.mVertices();
        final var normalBuffer = mesh.mNormals();

        // NOTE: There may be multiple UV sets, load just the first one
        final var uvBuffer = mesh.mTextureCoords(0);
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            final var position = vertexBuffer.get(i);
            final var normal = normalBuffer.get(i);
            final var textureCoordinates = uvBuffer.get(i);

            vertices[i] = new MeshVertex(new Vector3f(position.x(),
                                                      position.y(),
                                                      position.z()),
                                         new Vector3f(normal.x(),
                                                      normal.y(),
                                                      normal.z()),
                                         new Vector2f(textureCoordinates.x(),
                                                      textureCoordinates.y()));
        }

        // NOTE: Assumes that primitives are triangles.
        // NOTE: Assumes that short is large enough for indices
        final var indices = new Short[mesh.mNumFaces() * 3];

        final var faceBuffer = mesh.mFaces();
        for (int i = 0, count = 0; i < mesh.mNumFaces(); ++i) {
            final var face = faceBuffer.get(i);

            if (face.mNumIndices() != 3) {
                throw new IllegalStateException("Faces must be triangular! Got face with " + face.mNumIndices() + " indices, expected 3");
            }
            final var indexBuffer = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); ++j, ++count) {
                // NOTE: Assumes the meshes have at most ~32k (2^15) indices
                indices[count] = (short) indexBuffer.get(j);
            }
        }

        final Material material;
        final int materialIdx = mesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = DEFAULT_MATERIAL;
        }
        return new Mesh(this.deviceContext,
                        this.swapchain,
                        this.descriptorPool,
                        this.materialLayout,
                        this.sampler,
                        vertices,
                        indices,
                        material);
    }

    @Nullable
    private Texture tryGetTexture(final String path) {
        final var fullPath = this.assetRoot.resolve(path);

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() {
        this.defaultTexture.close();
    }
}
