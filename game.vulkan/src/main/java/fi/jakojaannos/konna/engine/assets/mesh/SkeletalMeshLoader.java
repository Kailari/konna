package fi.jakojaannos.konna.engine.assets.mesh;

import org.joml.*;
import org.lwjgl.assimp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import javax.annotation.Nullable;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.Material;
import fi.jakojaannos.konna.engine.assets.Mesh;
import fi.jakojaannos.konna.engine.assets.SkeletalMesh;
import fi.jakojaannos.konna.engine.assets.material.MaterialImpl;
import fi.jakojaannos.konna.engine.util.BitMask;
import fi.jakojaannos.konna.engine.vulkan.LogCategories;
import fi.jakojaannos.konna.engine.vulkan.RenderingBackend;
import fi.jakojaannos.konna.engine.vulkan.TextureSampler;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorPool;
import fi.jakojaannos.konna.engine.vulkan.descriptor.DescriptorSetLayout;

import static fi.jakojaannos.konna.engine.util.BitMask.bitMask;
import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiImportFile;

public class SkeletalMeshLoader extends MeshLoader<SkeletalMesh> {
    private static final Logger LOG = LoggerFactory.getLogger(SkeletalMeshLoader.class);

    private static final int MAX_WEIGHTS = 4;

    public SkeletalMeshLoader(final RenderingBackend backend, final AssetManager assetManager) {
        super(backend, assetManager);
    }

    @Override
    public Optional<SkeletalMesh> load(final Path path) {
        return load(path,
                    bitMask(AssimpProcess.JoinIdenticalVertices,
                            AssimpProcess.Triangulate,
                            AssimpProcess.LimitBoneWeights));
    }

    @Override
    public Optional<SkeletalMesh> load(final Path path, final BitMask<AssimpProcess> flags) {
        LOG.debug(LogCategories.MESH_LOADING, "Importing skeletal mesh \"{}\"",
                  path);
        final var scene = aiImportFile(this.assetManager.getRootPath()
                                                        .resolve(path)
                                                        .toString(),
                                       flags.mask());
        if (scene == null) {
            LOG.error("Error importing model: " + aiGetErrorString());
            return Optional.empty();
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
        final var bones = new ArrayList<MeshBone>();
        if (meshes != null) {
            for (int i = 0; i < numMeshes; i++) {
                final var mesh = AIMesh.create(meshes.get(i));
                final var processedMesh = processMesh(mesh, processedMaterials, bones);
                processedMeshes[i] = processedMesh;
            }
        }

        LOG.debug(LogCategories.MESH_LOADING, "\t-> Loaded {} submeshes with {} bones",
                  processedMaterials.size(),
                  bones.size());

        final var rootNode = scene.mRootNode();
        if (rootNode != null) {
            final var rootTransform = asMatrix(rootNode.mTransformation());
            final var rootAnimationNode = processNodeHierarchy(rootNode, null);
            final var animations = processAnimations(scene, bones, rootAnimationNode, rootTransform);
            return Optional.of(SkeletalMesh.from(animations, processedMeshes));
        } else {
            throw new IllegalStateException("No root node found on skeletal mesh!");
        }
    }

    private AnimationNode processNodeHierarchy(
            final AINode node,
            @Nullable final AnimationNode parent
    ) {
        final var nodeName = node.mName().dataString();
        final var animationNode = new AnimationNode(nodeName, parent);

        final var numChildren = node.mNumChildren();
        final var childBuffer = node.mChildren();
        for (int i = 0; i < numChildren; ++i) {
            assert childBuffer != null : "Child buffer should not be null if numChildren > 0";
            final var child = processNodeHierarchy(AINode.create(childBuffer.get(i)),
                                                   animationNode);
            animationNode.addChild(child);
        }

        return animationNode;
    }

    private Map<String, Animation> processAnimations(
            final AIScene scene,
            final List<MeshBone> bones,
            final AnimationNode rootNode,
            final Matrix4f rootTransform
    ) {
        final var animations = new HashMap<String, Animation>();

        final var numAnimations = scene.mNumAnimations();
        final var animationBuffer = scene.mAnimations();
        if (numAnimations == 0) {
            LOG.error("No animations on skeletal mesh.");
        }

        for (int animationIndex = 0; animationIndex < numAnimations; animationIndex++) {
            assert animationBuffer != null : "Animation buffer should not be null if numAnimations > 0";
            final var animation = AIAnimation.create(animationBuffer.get(animationIndex));

            final var numChannels = animation.mNumChannels();
            final var channelBuffer = animation.mChannels();
            if (numChannels == 0) {
                LOG.error("No channels on an animation.");
            }

            for (int channelIndex = 0; channelIndex < numChannels; channelIndex++) {
                assert channelBuffer != null : "Channel buffer should not be null if numChannels > 0";

                final var nodeAnimation = AINodeAnim.create(channelBuffer.get(channelIndex));
                final var nodeName = nodeAnimation.mNodeName().dataString();
                final var node = rootNode.findByName(nodeName);
                buildTransformationMatrices(nodeAnimation, node);
            }

            final var frames = buildAnimationFrames(bones, rootNode, rootTransform);
            final var processedAnimation = new Animation(animation.mName().dataString(),
                                                         frames,
                                                         animation.mDuration());
            animations.put(processedAnimation.name(), processedAnimation);
        }

        LOG.debug(LogCategories.MESH_LOADING, "\t-> Loaded {} animations with {} frames.",
                  animations.size(),
                  animations.values()
                            .stream()
                            .map(Animation::frames)
                            .mapToInt(List::size)
                            .toArray());
        return animations;
    }

    private List<Animation.Frame> buildAnimationFrames(
            final List<MeshBone> bones,
            final AnimationNode rootNode,
            final Matrix4f rootTransform
    ) {
        final var frames = new ArrayList<Animation.Frame>();

        final var frameKeys = new ArrayList<>(rootNode.getFrameSet());
        frameKeys.sort(Double::compareTo);
        for (final var frameKey : frameKeys) {
            final var numBones = bones.size();
            final var frame = new Animation.Frame(new Matrix4f[numBones]);
            frames.add(frame);

            for (int boneIndex = 0; boneIndex < numBones; boneIndex++) {
                final var bone = bones.get(boneIndex);
                final var node = rootNode.findByName(bone.name());
                final var boneMatrix = AnimationNode.getParentTransforms(node, frameKey);
                boneMatrix.mul(bone.transform());

                frame.boneTransforms()[boneIndex] = rootTransform.mul(boneMatrix, new Matrix4f());
            }
        }

        return frames;
    }

    private void buildTransformationMatrices(final AINodeAnim nodeAnimation, final AnimationNode node) {
        final var frameTranslations = new HashMap<Double, Vector3f>();
        final var frameRotations = new HashMap<Double, Quaternionf>();
        final var frameScalings = new HashMap<Double, Vector3f>();

        final var positionKeys = nodeAnimation.mPositionKeys();
        if (positionKeys != null) {
            final var numKeys = nodeAnimation.mNumPositionKeys();
            for (int i = 0; i < numKeys; i++) {
                final var key = positionKeys.get(i);
                frameTranslations.put(key.mTime(), new Vector3f(key.mValue().x(),
                                                                key.mValue().y(),
                                                                key.mValue().z()));
            }
        }

        final var rotationKeys = nodeAnimation.mRotationKeys();
        if (rotationKeys != null) {
            final var numKeys = nodeAnimation.mNumRotationKeys();
            for (int i = 0; i < numKeys; i++) {
                final var key = rotationKeys.get(i);
                frameRotations.put(key.mTime(),
                                   new Quaternionf(key.mValue().x(),
                                                   key.mValue().y(),
                                                   key.mValue().z(),
                                                   key.mValue().w()));
            }
        }

        final var scalingKeys = nodeAnimation.mScalingKeys();
        if (scalingKeys != null) {
            final var numKeys = nodeAnimation.mNumScalingKeys();
            for (int i = 0; i < numKeys; i++) {
                final var key = scalingKeys.get(i);
                frameScalings.put(key.mTime(), new Vector3f(key.mValue().x(),
                                                            key.mValue().y(),
                                                            key.mValue().z()));
            }
        }

        final var keySet = new HashSet<Double>();
        keySet.addAll(frameTranslations.keySet());
        keySet.addAll(frameRotations.keySet());
        keySet.addAll(frameScalings.keySet());

        final var frames = new HashMap<Double, FrameTransform>();
        for (final var key : keySet) {
            final var translation = frameTranslations.computeIfAbsent(key, ignored -> new Vector3f());
            final var rotation = frameRotations.computeIfAbsent(key, ignored -> new Quaternionf());
            final var scaling = frameScalings.computeIfAbsent(key, ignored -> new Vector3f(1.0f));

            final var matrix = new Matrix4f().translate(translation)
                                             .rotate(rotation)
                                             .scale(scaling);

            frames.put(key, new FrameTransform(translation, rotation, scaling, matrix));
        }

        node.setFrameTransforms(frames);
    }

    protected Mesh processMesh(
            final AIMesh mesh,
            final ArrayList<MaterialImpl> materials,
            final List<MeshBone> bones
    ) {
        final Material material;
        final int materialIdx = mesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = this.defaultMaterial;
        }

        final var boneWeights = processBones(mesh, bones);
        final var vertices = processVertices(mesh, boneWeights);
        final var indices = processIndices(mesh);
        return Mesh.from(this.backend,
                         SkeletalMeshVertex.FORMAT,
                         vertices,
                         indices,
                         material);
    }

    private BoneWeights[] processBones(final AIMesh mesh, final List<MeshBone> bones) {
        final var weightSet = new HashMap<Integer, List<VertexWeight>>();

        final var numBones = mesh.mNumBones();
        final var boneBuffer = mesh.mBones();
        if (boneBuffer == null) {
            LOG.error("\t-> No bones found");

            final var boneWeights = new BoneWeights[mesh.mNumVertices()];
            Arrays.fill(boneWeights, new BoneWeights(new Vector4i(-1), new Vector4f(0.0f)));
            return boneWeights;
        }

        for (int i = 0; i < numBones; ++i) {
            final var bone = AIBone.create(boneBuffer.get(i));
            final var id = bones.size();
            final var processedBone = new MeshBone(id, bone.mName().dataString(), asMatrix(bone.mOffsetMatrix()));
            bones.add(processedBone);

            final var numWeights = bone.mNumWeights();
            final var weightBuffer = bone.mWeights();
            for (int j = 0; j < numWeights; ++j) {
                final var weight = weightBuffer.get(j);
                final var vertexWeight = new VertexWeight(processedBone.id(),
                                                          weight.mVertexId(),
                                                          weight.mWeight());
                weightSet.computeIfAbsent(vertexWeight.vertexId(), key -> new ArrayList<>())
                         .add(vertexWeight);
            }
        }

        final var numVertices = mesh.mNumVertices();
        final var boneWeights = new BoneWeights[numVertices];
        for (int vertex = 0; vertex < numVertices; ++vertex) {
            boneWeights[vertex] = new BoneWeights(new Vector4i(-1), new Vector4f(0.0f));
            final var vertexWeights = weightSet.get(vertex);

            final int numWeights;
            if (vertexWeights != null) {
                // NOTE: This should not be necessary unless importer is ran with exotic parameters
                //       The default parameters already limit weights to maximum of four per vertex.
                vertexWeights.sort(Comparator.comparingDouble(VertexWeight::weight)
                                             .reversed());
                numWeights = vertexWeights.size();
            } else {
                numWeights = 0;
            }

            for (int j = 0; j < MAX_WEIGHTS; ++j) {
                if (j < numWeights) {
                    final var vertexWeight = vertexWeights.get(j);
                    boneWeights[vertex].set(j, vertexWeight.boneId(), vertexWeight.weight());
                } else {
                    boneWeights[vertex].set(j, 0, 0.0f);
                }
            }
        }

        return boneWeights;
    }

    private Matrix4f asMatrix(final AIMatrix4x4 mat) {
        // NOTE: This is actually transpose of the original matrix
        return new Matrix4f(mat.a1(), mat.b1(), mat.c1(), mat.d1(),
                            mat.a2(), mat.b2(), mat.c2(), mat.d2(),
                            mat.a3(), mat.b3(), mat.c3(), mat.d3(),
                            mat.a4(), mat.b4(), mat.c4(), mat.d4());
    }

    private SkeletalMeshVertex[] processVertices(final AIMesh mesh, final BoneWeights[] boneWeights) {
        final var vertices = new SkeletalMeshVertex[mesh.mNumVertices()];

        final var vertexBuffer = mesh.mVertices();
        final var normalBuffer = mesh.mNormals();

        // NOTE: There may be multiple UV sets, load just the first one
        final var uvBuffer = mesh.mTextureCoords(0);

        if (normalBuffer == null) {
            // FIXME: Fail gracefully
            throw new IllegalStateException("Mesh has no normals!");
        }
        if (uvBuffer == null) {
            // FIXME: Fail gracefully
            throw new IllegalStateException("Mesh has no UV coordinates!");
        }
        for (int vertex = 0; vertex < mesh.mNumVertices(); vertex++) {
            final var position = vertexBuffer.get(vertex);
            final var normal = normalBuffer.get(vertex);
            final var textureCoordinates = uvBuffer.get(vertex);

            vertices[vertex] = new SkeletalMeshVertex(new Vector3f(position.x(),
                                                                   position.y(),
                                                                   position.z()),
                                                      new Vector3f(normal.x(),
                                                                   normal.y(),
                                                                   normal.z()),
                                                      new Vector2f(textureCoordinates.x(),
                                                                   textureCoordinates.y()),
                                                      boneWeights[vertex].weights,
                                                      boneWeights[vertex].ids);
        }

        return vertices;
    }

    private record BoneWeights(Vector4i ids, Vector4f weights) {
        public void set(final int index, final int boneId, final float weight) {
            switch (index) {
                case 0 -> {
                    this.ids.x = boneId;
                    this.weights.x = weight;
                }
                case 1 -> {
                    this.ids.y = boneId;
                    this.weights.y = weight;
                }
                case 2 -> {
                    this.ids.z = boneId;
                    this.weights.z = weight;
                }
                case 3 -> {
                    this.ids.w = boneId;
                    this.weights.w = weight;
                }
                default -> throw new IllegalArgumentException("Index must be within range [0..3]");
            }
        }
    }

    private static record VertexWeight(int boneId, int vertexId, float weight) {}
}
