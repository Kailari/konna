package fi.jakojaannos.konna.assets.loader;

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

import fi.jakojaannos.konna.assets.AssimpProcess;
import fi.jakojaannos.konna.assets.Material;
import fi.jakojaannos.konna.assets.Texture;
import fi.jakojaannos.konna.util.BitMask;
import fi.jakojaannos.konna.vulkan.LogCategories;
import fi.jakojaannos.konna.vulkan.TextureSampler;
import fi.jakojaannos.konna.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.vulkan.descriptor.DescriptorSetLayout;
import fi.jakojaannos.konna.vulkan.device.DeviceContext;
import fi.jakojaannos.konna.vulkan.rendering.Swapchain;

import static fi.jakojaannos.konna.util.BitMask.bitMask;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public abstract class MeshLoader<TMesh> {
    private static final Logger LOG = LoggerFactory.getLogger(MeshLoader.class);

    protected final DeviceContext deviceContext;
    protected final Swapchain swapchain;
    protected final DescriptorPool descriptorPool;
    protected final DescriptorSetLayout descriptorLayout;
    protected final TextureSampler textureSampler;

    protected final Path assetRoot;
    protected final Texture defaultTexture;
    protected final Material defaultMaterial;

    protected MeshLoader(
            final DeviceContext deviceContext,
            final Swapchain swapchain,
            final DescriptorPool descriptorPool,
            final DescriptorSetLayout descriptorLayout,
            final TextureSampler textureSampler,
            final Texture defaultTexture,
            final Path assetRoot
    ) {
        this.deviceContext = deviceContext;
        this.swapchain = swapchain;
        this.descriptorPool = descriptorPool;
        this.descriptorLayout = descriptorLayout;
        this.textureSampler = textureSampler;

        this.assetRoot = assetRoot;
        this.defaultTexture = defaultTexture;
        this.defaultMaterial = new Material(Material.DEFAULT_COLOR,
                                            Material.DEFAULT_COLOR,
                                            Material.DEFAULT_COLOR,
                                            this.defaultTexture,
                                            false,
                                            0.0f);
    }

    public TMesh load(final Path path) {
        return load(path,
                    bitMask(AssimpProcess.JoinIdenticalVertices,
                            AssimpProcess.Triangulate));
    }

    public abstract TMesh load(final Path path, final BitMask<AssimpProcess> flags);

    protected void processMaterial(
            final AIMaterial material,
            final ArrayList<Material> processedMaterials
    ) {
        LOG.trace(LogCategories.MESH_LOADING, "\t-> Processing material");
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
            } else {
                hasTexture = false;
                texture = this.defaultTexture;
            }

            // Try fetching the ambient color
            final Vector4f ambient;
            if (aiGetMaterialColor(material, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                ambient = new Vector4f(color.r(), color.g(), color.b(), color.a());
            } else {
                ambient = Material.DEFAULT_COLOR;
            }

            // Try fetching the diffuse color
            final Vector4f diffuse;
            if (aiGetMaterialColor(material, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                diffuse = new Vector4f(color.r(), color.g(), color.b(), color.a());
            } else {
                diffuse = Material.DEFAULT_COLOR;
            }

            // Try fetching the diffuse color
            final Vector4f specular;
            if (aiGetMaterialColor(material, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color) == aiReturn_SUCCESS) {
                specular = new Vector4f(color.r(), color.g(), color.b(), color.a());
            } else {
                specular = Material.DEFAULT_COLOR;
            }

            LOG.trace(LogCategories.MESH_LOADING, "\t\t-> texture: {}, ambient: {}, diffuse: {}, specular: {}",
                      hasTexture ? path.dataString() : "No texture",
                      formatColor(ambient),
                      formatColor(diffuse),
                      formatColor(specular));

            final var processed = new Material(ambient,
                                               diffuse,
                                               specular,
                                               texture,
                                               hasTexture,
                                               1.0f);
            processedMaterials.add(processed);
        }
    }

    private String formatColor(final Vector4f color) {
        return color != Material.DEFAULT_COLOR
                ? String.format("(%.2f, %.2f, %.2f, %.2f)", color.x, color.y, color.z, color.w)
                : "Default";
    }

    protected Integer[] processIndices(final AIMesh mesh) {
        // NOTE: Assumes that primitives are triangles.
        // NOTE: Assumes that short is large enough for indices
        final var indices = new Integer[mesh.mNumFaces() * 3];

        final var faceBuffer = mesh.mFaces();
        for (int i = 0, count = 0; i < mesh.mNumFaces(); ++i) {
            final var face = faceBuffer.get(i);

            if (face.mNumIndices() != 3) {
                throw new IllegalStateException("Faces must be triangular! Got face with " + face.mNumIndices() + " indices, expected 3");
            }
            final var indexBuffer = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); ++j, ++count) {
                // NOTE: Assumes the meshes have at most ~32k (2^15) indices
                indices[count] = indexBuffer.get(j);
            }
        }

        return indices;
    }

    @Nullable
    private Texture tryGetTexture(final String path) {
        final var fullPath = this.assetRoot.resolve(path);

        throw new UnsupportedOperationException("Not implemented");
    }
}
