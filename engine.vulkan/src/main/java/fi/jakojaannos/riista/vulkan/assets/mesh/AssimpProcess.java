package fi.jakojaannos.riista.vulkan.assets.mesh;

import fi.jakojaannos.riista.utilities.BitFlags;
import fi.jakojaannos.riista.utilities.BitMask;

import static fi.jakojaannos.riista.utilities.BitMask.bitMask;
import static org.lwjgl.assimp.Assimp.*;

public enum AssimpProcess implements BitFlags {
    CalcTangentSpace(aiProcess_CalcTangentSpace),
    JoinIdenticalVertices(aiProcess_JoinIdenticalVertices),
    MakeLeftHanded(aiProcess_MakeLeftHanded),
    Triangulate(aiProcess_Triangulate),
    RemoveComponent(aiProcess_RemoveComponent),
    GenNormals(aiProcess_GenNormals),
    GenSmoothNormals(aiProcess_GenSmoothNormals),
    SplitLargeMeshes(aiProcess_SplitLargeMeshes),
    PreTransformVertices(aiProcess_PreTransformVertices),
    LimitBoneWeights(aiProcess_LimitBoneWeights),
    ValidateDataStructure(aiProcess_ValidateDataStructure),
    ImproveCacheLocality(aiProcess_ImproveCacheLocality),
    RemoveRedundantMaterials(aiProcess_RemoveRedundantMaterials),
    FixInfacingNormals(aiProcess_FixInfacingNormals),
    SortByPType(aiProcess_SortByPType),
    FindDegenerates(aiProcess_FindDegenerates),
    FindInvalidData(aiProcess_FindInvalidData),
    GenUVCoords(aiProcess_GenUVCoords),
    TransformUVCoords(aiProcess_TransformUVCoords),
    FindInstances(aiProcess_FindInstances),
    OptimizeMeshes(aiProcess_OptimizeMeshes),
    OptimizeGraph(aiProcess_OptimizeGraph),
    FlipUVs(aiProcess_FlipUVs),
    FlipWindingOrder(aiProcess_FlipWindingOrder),
    SplitByBoneCount(aiProcess_SplitByBoneCount),
    Debone(aiProcess_Debone),
    GlobalScale(aiProcess_GlobalScale),
    EmbedTextures(aiProcess_EmbedTextures),
    ForceGenNormals(aiProcess_ForceGenNormals),
    DropNormals(aiProcess_DropNormals),
    GenBoundingBoxes(aiProcess_GenBoundingBoxes);

    private final int mask;

    @Override
    public int getMask() {
        return this.mask;
    }

    AssimpProcess(final int mask) {
        this.mask = mask;
    }

    public static final class Preset {
        public static final BitMask<AssimpProcess> ConvertToLeftHanded = bitMask(MakeLeftHanded,
                                                                                 FlipUVs,
                                                                                 FlipWindingOrder);
        public static final BitMask<AssimpProcess> TargetRealtime_Fast = bitMask(CalcTangentSpace,
                                                                                 GenNormals,
                                                                                 JoinIdenticalVertices,
                                                                                 Triangulate,
                                                                                 GenUVCoords,
                                                                                 SortByPType);
        public static final BitMask<AssimpProcess> TargetRealtime_Quality = bitMask(CalcTangentSpace,
                                                                                    GenSmoothNormals,
                                                                                    JoinIdenticalVertices,
                                                                                    ImproveCacheLocality,
                                                                                    LimitBoneWeights,
                                                                                    RemoveRedundantMaterials,
                                                                                    SplitLargeMeshes,
                                                                                    Triangulate,
                                                                                    GenUVCoords,
                                                                                    SortByPType,
                                                                                    FindDegenerates,
                                                                                    FindInvalidData);
        public static final BitMask<AssimpProcess> TargetRealtime_MaxQuality = bitMask(CalcTangentSpace,
                                                                                       GenSmoothNormals,
                                                                                       JoinIdenticalVertices,
                                                                                       ImproveCacheLocality,
                                                                                       LimitBoneWeights,
                                                                                       RemoveRedundantMaterials,
                                                                                       SplitLargeMeshes,
                                                                                       Triangulate,
                                                                                       GenUVCoords,
                                                                                       SortByPType,
                                                                                       FindDegenerates,
                                                                                       FindInvalidData,
                                                                                       FindInstances,
                                                                                       FindInvalidData,
                                                                                       OptimizeMeshes);
    }
}
