package org.example.exmod.mesh;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.PauseableThread;
import com.github.puzzle.game.worldgen.structures.Structure;
import com.github.puzzle.util.Vec3i;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.ImmutablePair;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import finalforeach.cosmicreach.RuntimeInfo;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.RenderOrder;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.util.ArrayUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LoneThread {

    LoneThreadRunnable runnable = new LoneThreadRunnable();

    private static final Array<MeshData> emptyMeshDatas = new Array(false, 0, MeshData.class);
    public boolean started;

    private static Structure getStructureAtVec(Map<Vec3i, Structure> structureMap, Vec3i vec3i) {
        int cx = Math.floorDiv(vec3i.x(), 16);
        int cy = Math.floorDiv(vec3i.y(), 16);
        int cz = Math.floorDiv(vec3i.z(), 16);

        return structureMap.get(new Vec3i(cx, cy, cz));
    }

    public static BlockState getBlockState(Map<Vec3i, Structure> structureMap, Structure candidateChunk, Vec3i pos, int x, int y, int z) {
        return get(structureMap, candidateChunk, pos, x, y, z, Structure::getBlockState);
    }

    public static <T> T get(Map<Vec3i, Structure> structureMap, Structure candidateChunk, Vec3i pos, int x, int y, int z, BlockPositionFunction<T> function) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Structure c;
        if (candidateChunk != null && pos.x() == cx && pos.y() == cy && pos.z() == cz) {
            c = candidateChunk;
        } else {
            c = getStructureAtVec(structureMap, new Vec3i(x, y, z));
        }

        if (c == null) {
            return null;
        } else {
            x -= 16 * cx;
            y -= 16 * cy;
            z -= 16 * cz;
            return function.apply(c, x, y, z);
        }
    }

    private static BlockState getBlockState(Map<Vec3i, Structure> structureMap, Pair<Vec3i, Structure> a, Pair<Vec3i, Structure> b, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Structure c;
        if (a != null && a.getLeft().x() == cx && a.getLeft().y() == cy && a.getLeft().z() == cz) {
            c = a.getRight();
        } else if (b != null && b.getLeft().x() == cx && b.getLeft().y() == cy && b.getLeft().z() == cz) {
            c = b.getRight();
        } else {
            c = getStructureAtVec(structureMap, new Vec3i(x, y, z));
        }

        if (c == null) {
            return null;
        } else {
            x -= 16 * cx;
            y -= 16 * cy;
            z -= 16 * cz;
            return c.getBlockState(x, y, z);
        }
    }

    static Array<MeshData> getMeshData(Map<Vec3i, Structure> structureMap, Vec3i pos, Structure chunk) {
        BlockState airBlockState = Block.AIR.getDefaultBlockState();
        Pair<Vec3i, Structure> structurePair = new ImmutablePair<>(pos, chunk);
        if (chunk.isEntirely(airBlockState)) {
            return emptyMeshDatas;
        } else {
            boolean isEntirelyOpaque = chunk.isEntirelyOpaque();
            if (isEntirelyOpaque && chunk.isCulledByAdjacentChunks(pos, structureMap)) {
                return emptyMeshDatas;
            } else {
                Array<MeshData> meshDatas = new Array(3);
                boolean isEntirelyOneBlockSelfCulling = chunk.isEntirelyOneBlockSelfCulling();
                short[] blockLightLevels = new short[8];
                int[] skyLightLevels = new int[8];

                Arrays.fill(blockLightLevels, (short) 4095);

                Pair<Vec3i, Structure> chunkNegX = structureMap.get(new Vec3i(pos.x() - 1, pos.y(), pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x() - 1, pos.y(), pos.z()), structureMap.get(new Vec3i(pos.x() - 1, pos.y(), pos.z()))) : null;
                Pair<Vec3i, Structure> chunkPosX = structureMap.get(new Vec3i(pos.x() + 1, pos.y(), pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x() + 1, pos.y(), pos.z()), structureMap.get(new Vec3i(pos.x() + 1, pos.y(), pos.z()))) : null;
                Pair<Vec3i, Structure> chunkNegY = structureMap.get(new Vec3i(pos.x(), pos.y() - 1, pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y() - 1, pos.z()), structureMap.get(new Vec3i(pos.x(), pos.y() - 1, pos.z()))) : null;
                Pair<Vec3i, Structure> chunkPosY = structureMap.get(new Vec3i(pos.x(), pos.y() + 1, pos.z())) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y() + 1, pos.z()), structureMap.get(new Vec3i(pos.x(), pos.y() + 1, pos.z()))) : null;
                Pair<Vec3i, Structure> chunkNegZ = structureMap.get(new Vec3i(pos.x(), pos.y(), pos.z() - 1)) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y(), pos.z() - 1), structureMap.get(new Vec3i(pos.x(), pos.y(), pos.z() - 1))) : null;
                Pair<Vec3i, Structure> chunkPosZ = structureMap.get(new Vec3i(pos.x(), pos.y(), pos.z() + 1)) != null ? new ImmutablePair<>(new Vec3i(pos.x(), pos.y(), pos.z() + 1), structureMap.get(new Vec3i(pos.x(), pos.y(), pos.z() + 1))) : null;

                for(int idx = 0; idx < 4096; idx++) {
                    int localY = idx / 256;
                    int localX = (idx - localY * 256) / 16;
                    int localZ = (idx - localY * 256) % 16;
                    if ((isEntirelyOpaque || isEntirelyOneBlockSelfCulling) && localX != 0 && localX != 15 && localY != 0 && localY != 15 && localZ != 0 && localZ != 15) {
                        idx += 13;
                    } else {
                        BlockState b = chunk.getBlockState(localX, localY, localZ);
                        if (b != airBlockState) {
                            int globalX = (pos.x() * 16) + localX;
                            int globalY = (pos.y() * 16) + localY;
                            int globalZ = (pos.z() * 16) + localZ;
                            BlockState bnx = getBlockState(structureMap, structurePair, chunkNegX, globalX - 1, globalY, globalZ);
                            BlockState bpx = getBlockState(structureMap, structurePair, chunkPosX, globalX + 1, globalY, globalZ);
                            BlockState bny = getBlockState(structureMap, structurePair, chunkNegY, globalX, globalY - 1, globalZ);
                            BlockState bpy = getBlockState(structureMap, structurePair, chunkPosY, globalX, globalY + 1, globalZ);
                            BlockState bnz = getBlockState(structureMap, structurePair, chunkNegZ, globalX, globalY, globalZ - 1);
                            BlockState bpz = getBlockState(structureMap, structurePair, chunkPosZ, globalX, globalY, globalZ + 1);
                            int completeCullMask = 0;
                            completeCullMask |= bnx != null && !bnx.isPosXFaceOccluding ? 0 : 1;
                            completeCullMask |= bpx != null && !bpx.isNegXFaceOccluding ? 0 : 2;
                            completeCullMask |= bny != null && !bny.isPosYFaceOccluding ? 0 : 4;
                            completeCullMask |= bpy != null && !bpy.isNegYFaceOccluding ? 0 : 8;
                            completeCullMask |= bnz != null && !bnz.isPosZFaceOccluding ? 0 : 16;
                            completeCullMask |= bpz != null && !bpz.isNegZFaceOccluding ? 0 : 32;
                            if (completeCullMask != 63) {
                                int opaqueBitmask = 0;
                                Block block = b.getBlock();
                                boolean cullsSelf = b.cullsSelf;
                                opaqueBitmask |= bnx != null && !bnx.isPosXFaceOccluding && (!cullsSelf || block != bnx.getBlock() || !bnx.isSelfPosXFaceOccluding) ? 0 : 1;
                                opaqueBitmask |= bpx != null && !bpx.isNegXFaceOccluding && (!cullsSelf || block != bpx.getBlock() || !bpx.isSelfNegXFaceOccluding) ? 0 : 2;
                                opaqueBitmask |= bny != null && !bny.isPosYFaceOccluding && (!cullsSelf || block != bny.getBlock() || !bny.isSelfPosYFaceOccluding) ? 0 : 4;
                                opaqueBitmask |= bpy != null && !bpy.isNegYFaceOccluding && (!cullsSelf || block != bpy.getBlock() || !bpy.isSelfNegYFaceOccluding) ? 0 : 8;
                                opaqueBitmask |= bnz != null && !bnz.isPosZFaceOccluding && (!cullsSelf || block != bnz.getBlock() || !bnz.isSelfPosZFaceOccluding) ? 0 : 16;
                                opaqueBitmask |= bpz != null && !bpz.isNegZFaceOccluding && (!cullsSelf || block != bpz.getBlock() || !bpz.isSelfNegZFaceOccluding) ? 0 : 32;
                                BlockState bnxnynz = getBlockState(structureMap, chunk, pos, globalX - 1, globalY - 1, globalZ - 1);
                                BlockState bnxny0z = getBlockState(structureMap, chunk, pos, globalX - 1, globalY - 1, globalZ);
                                BlockState bnxnypz = getBlockState(structureMap, chunk, pos, globalX - 1, globalY - 1, globalZ + 1);
                                BlockState bnx0ynz = getBlockState(structureMap, chunk, pos, globalX - 1, globalY, globalZ - 1);
                                BlockState bnx0ypz = getBlockState(structureMap, chunk, pos, globalX - 1, globalY, globalZ + 1);
                                BlockState bnxpynz = getBlockState(structureMap, chunk, pos, globalX - 1, globalY + 1, globalZ - 1);
                                BlockState bnxpy0z = getBlockState(structureMap, chunk, pos, globalX - 1, globalY + 1, globalZ);
                                BlockState bnxpypz = getBlockState(structureMap, chunk, pos, globalX - 1, globalY + 1, globalZ + 1);
                                BlockState b0xnynz = getBlockState(structureMap, chunk, pos, globalX, globalY - 1, globalZ - 1);
                                BlockState b0xnypz = getBlockState(structureMap, chunk, pos, globalX, globalY - 1, globalZ + 1);
                                BlockState b0xpynz = getBlockState(structureMap, chunk, pos, globalX, globalY + 1, globalZ - 1);
                                BlockState b0xpypz = getBlockState(structureMap, chunk, pos, globalX, globalY + 1, globalZ + 1);
                                BlockState bpxnynz = getBlockState(structureMap, chunk, pos, globalX + 1, globalY - 1, globalZ - 1);
                                BlockState bpxny0z = getBlockState(structureMap, chunk, pos, globalX + 1, globalY - 1, globalZ);
                                BlockState bpxnypz = getBlockState(structureMap, chunk, pos, globalX + 1, globalY - 1, globalZ + 1);
                                BlockState bpx0ynz = getBlockState(structureMap, chunk, pos, globalX + 1, globalY, globalZ - 1);
                                BlockState bpx0ypz = getBlockState(structureMap, chunk, pos, globalX + 1, globalY, globalZ + 1);
                                BlockState bpxpynz = getBlockState(structureMap, chunk, pos, globalX + 1, globalY + 1, globalZ - 1);
                                BlockState bpxpy0z = getBlockState(structureMap, chunk, pos, globalX + 1, globalY + 1, globalZ);
                                BlockState bpxpypz = getBlockState(structureMap, chunk, pos, globalX + 1, globalY + 1, globalZ + 1);
                                opaqueBitmask |= bnxnynz != null && bnxnynz.isOpaque ? 64 : 0;
                                opaqueBitmask |= bnxny0z != null && bnxny0z.isOpaque ? 128 : 0;
                                opaqueBitmask |= bnxnypz != null && bnxnypz.isOpaque ? 256 : 0;
                                opaqueBitmask |= bnx0ynz != null && bnx0ynz.isOpaque ? 512 : 0;
                                opaqueBitmask |= bnx0ypz != null && bnx0ypz.isOpaque ? 1024 : 0;
                                opaqueBitmask |= bnxpynz != null && bnxpynz.isOpaque ? 2048 : 0;
                                opaqueBitmask |= bnxpy0z != null && bnxpy0z.isOpaque ? 4096 : 0;
                                opaqueBitmask |= bnxpypz != null && bnxpypz.isOpaque ? 8192 : 0;
                                opaqueBitmask |= b0xnynz != null && b0xnynz.isOpaque ? 16384 : 0;
                                opaqueBitmask |= b0xnypz != null && b0xnypz.isOpaque ? '耀' : 0;
                                opaqueBitmask |= b0xpynz != null && b0xpynz.isOpaque ? 65536 : 0;
                                opaqueBitmask |= b0xpypz != null && b0xpypz.isOpaque ? 131072 : 0;
                                opaqueBitmask |= bpxnynz != null && bpxnynz.isOpaque ? 262144 : 0;
                                opaqueBitmask |= bpxny0z != null && bpxny0z.isOpaque ? 524288 : 0;
                                opaqueBitmask |= bpxnypz != null && bpxnypz.isOpaque ? 1048576 : 0;
                                opaqueBitmask |= bpx0ynz != null && bpx0ynz.isOpaque ? 2097152 : 0;
                                opaqueBitmask |= bpx0ypz != null && bpx0ypz.isOpaque ? 4194304 : 0;
                                opaqueBitmask |= bpxpynz != null && bpxpynz.isOpaque ? 8388608 : 0;
                                opaqueBitmask |= bpxpy0z != null && bpxpy0z.isOpaque ? 16777216 : 0;
                                opaqueBitmask |= bpxpypz != null && bpxpypz.isOpaque ? 33554432 : 0;
                                MeshData md = null;
                                GameShader shader = GameShader.getShaderForBlockState(b);
                                RenderOrder renderOrder = RenderOrder.getRenderOrderForBlockState(b);

                                for(int i = 0; i < meshDatas.size; ++i) {
                                    MeshData candidate = meshDatas.get(i);
                                    if (candidate.shader == shader && candidate.renderOrder == renderOrder) {
                                        md = candidate;
                                        break;
                                    }
                                }

                                if (md == null) {
                                    md = new MeshData(new FloatArray(1024), RuntimeInfo.useSharedIndices ? null : new IntArray(), shader, renderOrder);
                                    meshDatas.add(md);
                                }

                                b.addVertices(md, globalX - (pos.x() * 16), globalY - (pos.y() * 16), globalZ - (pos.z() * 16), opaqueBitmask, blockLightLevels, skyLightLevels);
                            }
                        }
                    }
                }
                ArrayUtils.removeIf(meshDatas, (mdx) -> mdx.vertices.isEmpty());
                return meshDatas;
            }
        }
    }

    public PauseableThread pauseableThread;

    public LoneThread() {
        this.pauseableThread = Threads.createPauseableThread("WorldRenderingMeshGenThread", this.runnable);
    }

    public void postRunnable(Runnable runnable) {
        if (!this.started) {
            this.pauseableThread.start();
            this.started = true;
        } else {
            this.pauseableThread.onResume();
        }

        this.runnable.runners.addLast(runnable);
    }

    public void meshChunk(Map<Vec3i, Structure> structureMap, Vec3i pos, Structure structure, AtomicReference<Array<MeshData>> dataAtomicReference) {
        if (!this.started) {
            this.pauseableThread.start();
            this.started = true;
        } else {
            this.pauseableThread.onResume();
        }

        this.runnable.runners.addLast(() -> {
            try {
//                Array<MeshData> array = new Array<>();
//                array.add(meshFromStructure(structure));
                Array<MeshData> array = getMeshData(structureMap, pos, structure);
                dataAtomicReference.set(array);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        });
    }

}
