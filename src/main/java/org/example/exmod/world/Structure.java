package org.example.exmod.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.constants.Direction;
import org.example.exmod.world.lighting.BlockLightPropagator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Structure {

    final short version;
    final Identifier id;
    final Vector3 chunkPos;
    final Vector3 blockPos;

    final List<String> palette;
    public StructureWorld parent;

    // int blocks
    // blocksIndex, chunk x y z, bp x y z
    short[] positions;
    public short[] blockLights;

    public boolean needsRemeshing = true;
    public boolean needsToRebuildMesh = true;

    public Structure(
            short version,
            Identifier id
    ) {
        this.palette = new ArrayList<>(0);
        palette.add("base:air[default]");
        this.version = version;
        this.id = id;

        this.positions = new short[16 * 16 * 16];
        this.blockLights = new short[16 * 16 * 16];

        chunkPos = new Vector3(0, 0, 0);
        blockPos = new Vector3(0, 0, 0);
    }

    Structure(short version, String id, List<String> blocks, short[] positions) {
        this.version = version;
        this.palette = blocks;
        this.id = Identifier.fromString(id);
        this.positions = positions;

        chunkPos = new Vector3(0, 0, 0);
        blockPos = new Vector3(0, 0, 0);
    }

    public void setChunkPos(Vec3i pos) {
        chunkPos.set(pos.x(), pos.y(), pos.z());
        blockPos.set(pos.x() * 16, pos.y() * 16, pos.z() * 16);
    }

    public void setParentWorld(StructureWorld world) {
        this.parent = world;
    }

    public Vec3i getChunkPos() {
        return new Vec3i((int) chunkPos.x, (int) chunkPos.y, (int) chunkPos.z);
    }

    public Vec3i getBlockPos() {
        return new Vec3i((int) blockPos.x, (int) blockPos.y, (int) blockPos.z);
    }

    public void flagTouchingChunksForRemeshing(StructureWorld zone, int localX, int localY, int localZ, boolean updateImmediately) {
        this.flagForRemeshing(updateImmediately);
        if (localX == 0 || localY == 0 || localZ == 0 || localX == 15 || localY == 15 || localZ == 15) {
            int dx = localX == 0 ? -1 : (localX == 15 ? 1 : 0);
            int dy = localY == 0 ? -1 : (localY == 15 ? 1 : 0);
            int dz = localZ == 0 ? -1 : (localZ == 15 ? 1 : 0);
            Structure nc;
            if (dx != 0) {
                nc = zone.getChunkAtChunkCoords((int) (this.chunkPos.x + dx), (int) this.chunkPos.y, (int) this.chunkPos.z);
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }

            if (dy != 0) {
                nc = zone.getChunkAtChunkCoords((int) this.chunkPos.x, (int) (this.chunkPos.y + dy), (int) this.chunkPos.z);
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }

            if (dz != 0) {
                nc = zone.getChunkAtChunkCoords((int) this.chunkPos.x, (int) this.chunkPos.y, (int) (this.chunkPos.z + dz));
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }

            if (dx != 0 && dy != 0) {
                nc = zone.getChunkAtChunkCoords((int) (this.chunkPos.x + dx), (int) (this.chunkPos.y + dy), (int) this.chunkPos.z);
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }

            if (dx != 0 && dz != 0) {
                nc = zone.getChunkAtChunkCoords((int) (this.chunkPos.x + dx), (int) this.chunkPos.y, (int) (this.chunkPos.z + dz));
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }

            if (dy != 0 && dz != 0) {
                nc = zone.getChunkAtChunkCoords((int) this.chunkPos.x, (int) (this.chunkPos.y + dy), (int) (this.chunkPos.z + dz));
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }

            if (dx != 0 && dy != 0 && dz != 0) {
                nc = zone.getChunkAtChunkCoords((int) (this.chunkPos.x + dx), (int) (this.chunkPos.y + dy), (int) (this.chunkPos.z + dz));
                if (nc != null) {
                    nc.flagForRemeshing(updateImmediately);
                }
            }
        }

    }

    private void flagForRemeshing(boolean updateImmediately) {
        this.needsRemeshing = true;
    }

    short addBlock(String blockId) {
        if (!palette.contains(blockId)) {
            palette.add(blockId);
            return (short) (palette.size() - 1);
        }
        return (short) palette.indexOf(blockId);
    }

    public static int to1DCoords(int x, int y, int z) {
        return (z * 16 * 16) + (y * 16) + x;
    }

    public static int to1DCoords(Vec3i vec3i) {
        return (vec3i.z() * 16 * 16) + (vec3i.y() * 16) + vec3i.x();
    }

    public static Vec3i to3DCoords(int index) {
        int tmpIndex = index;

        int z = tmpIndex / (16 * 16);
        tmpIndex -= (z * 16 * 16);
        int y = tmpIndex / 16;
        int x = tmpIndex % 16;
        return new Vec3i(x, y, z);
    }

    public void setBlockState(BlockState state, int x, int y, int z) {
        positions[to1DCoords(x, y, z)] = addBlock(state.getSaveKey());
        if (state.isLightEmitter()) {
            setBlockLight(
                    state.lightLevelRed, state.lightLevelGreen, state.lightLevelBlue,
                    x, y, z
            );
        }
    }

    public void setBlockLight(int r, int g, int b, int x, int y, int z) {
        short blockLight = (short) ((r << 8) + (g << 4) + b);
        blockLights[to1DCoords(x, y, z)] = blockLight;

        if (parent != null) {
            Queue<BlockPos> queue = new Queue<>();
            queue.addLast(new BlockPos(this, x, y, z));

//            BlockLightPropagator.propagateBlockDarkness(parent, queue);
//            BlockLightPropagator.propagateBlockLights(parent, queue);
        }
    }

    static Map<String, BlockState> blockStateCache = new HashMap<>();

    public BlockState getBlockState(int x, int y, int z) {
        return
                x >= 0 && y >= 0 && z >= 0 && x < 16 && y < 16 && z < 16 ?
                        getInstance(palette.get(positions[to1DCoords(x, y, z)]))
                        : null;
    }

    public void prunePalette() {
        List<String> oldPalette = new ArrayList<>(palette);
        palette.clear();
        for (int i = 0; i < positions.length; i++) {
            palette.add(oldPalette.get(positions[i]));
            positions[i] = (short) (palette.size() - 1);
        }
        oldPalette.clear();
    }

    public boolean isEntirely(BlockState state) {
        prunePalette();
        return (palette.size() == 1) && (Objects.equals(palette.get(0), state.getSaveKey()));
    }

    public boolean isEntirely(Predicate<BlockState> statePredicate) {
        for (int idx : positions) {
            BlockState state = getInstance(palette.get(idx));

            if (!statePredicate.test(state)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEntirelyOpaque() {
        return isEntirely((b) -> b != null && b.isOpaque);
    }

    public boolean isEntirelyOneBlockSelfCulling() {
        prunePalette();
        return (palette.size() == 1) && isEntirely((b) -> b != null && b.cullsSelf);
    }

    public boolean isCulledByAdjacentChunks(Vec3i pos, StructureWorld zone) {
        for (Direction d : Direction.ALL_DIRECTIONS) {
            Structure n = zone.getChunkAtChunkCoords(new Vec3i(pos.x() + d.getXOffset(), pos.y() + d.getYOffset(), pos.z() + d.getZOffset()));
            if (n == null || !n.isEntirelyOpaque()) {
                return false;
            }
        }

        return true;
    }

    public int getMaxNonEmptyBlockIdxYXZ() {
        return 4096;
    }

    public static BlockState getInstance(String str) {
        if (blockStateCache.containsKey(str)) return blockStateCache.get(str);
        blockStateCache.put(str, BlockState.getInstance(str));
        return blockStateCache.get(str);
    };

    public void foreach(BiConsumer<Vec3i, BlockState> blockConsumer) {
        for (int i = 0; i < positions.length; i++) {
            int idx = positions[i];
            blockConsumer.accept(
                    to3DCoords(i),
                    getInstance(palette.get(idx))
            );
        }
    }

    public static Structure readFromStream(DataInputStream stream) throws IOException {
        // Read Header
        short version = stream.readShort();
        switch (StructureFormat.values()[version]) {
            case BLOCKS_ONLY: return readVersion0(version, stream);
            default: throw new RuntimeException("Invalid structure version");
        }
    }

    static Structure readVersion0(short version, DataInputStream stream) throws IOException {
        String id = stream.readUTF();
        List<String> blocks = new ArrayList<>();

        int blockListSize = stream.readInt();
        for (int i = 0; i < blockListSize - 1; i++) {
            blocks.add(stream.readUTF());
        }

        int size = stream.readInt();

        short[] bytes = new short[size];
        for (int i = 0; i < size; i++) {
            short index = stream.readShort();

            int bx = stream.readUnsignedByte();
            int by = stream.readUnsignedByte();
            int bz = stream.readUnsignedByte();

            if (index != 0) {
                bytes[to1DCoords(bx, by, bz)] = index;
            }
        }

        return new Structure(version, id, blocks, bytes);
    }

    public void writeToStream(DataOutputStream stream) throws IOException {
        // Write Header
        stream.writeShort(version);
        switch (StructureFormat.values()[version]) {
            case BLOCKS_ONLY: writeVersion0(this, stream);
            default: throw new RuntimeException("Invalid structure version");
        }

    }

    static void writeVersion0(Structure structure, DataOutputStream stream) throws IOException {
        stream.writeUTF(structure.id.toString());

        // Write Block Palette
        stream.writeInt(structure.palette.size());
        for (String block : structure.palette) stream.writeUTF(block);

        // Write Structure Data
        stream.writeInt(structure.positions.length);
        for (int i = 0; i < structure.positions.length; i++) {
            stream.writeShort(structure.positions[i]);

            Vec3i coords = to3DCoords(i);
            stream.writeByte(coords.x());
            stream.writeByte(coords.y());
            stream.writeByte(coords.z());
        }
    }

    public short getBlockLight(int localX, int localY, int localZ) {
        return blockLights[to1DCoords(localX, localY, localZ)];
    }
}
