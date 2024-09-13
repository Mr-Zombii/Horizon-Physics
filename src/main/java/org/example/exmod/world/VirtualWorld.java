package org.example.exmod.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.util.Vec3i;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import finalforeach.cosmicreach.blocks.BlockState;
import org.example.exmod.Constants;
import org.example.exmod.mesh.BlockPositionFunction;
import org.example.exmod.util.CollisionMeshUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VirtualWorld {

    Map<Vec3i, VirtualChunk> structureMap = new HashMap<>();
    public BoundingBox AABB = new BoundingBox();
    public CompoundCollisionShape CCS = new CompoundCollisionShape();

    public VirtualWorld() {}

    public boolean hasCollisionShape = false;
    public boolean nullSideChunks = false;

    public static final VirtualChunk emptyStructure = new EmptyVirtualChunk((short) 0, new Vec3i(0, 0, 0), new Identifier(Constants.MOD_ID, "empty"));

    public VirtualChunk getChunkAtBlock(int x, int y, int z) {
        return getChunkAtBlock(new Vec3i(x, y, z));
    }

    public VirtualChunk getChunkAtBlock(Vec3i blockPos) {
        int cx = Math.floorDiv(blockPos.x(), 16);
        int cy = Math.floorDiv(blockPos.y(), 16);
        int cz = Math.floorDiv(blockPos.z(), 16);

        return structureMap.get(new Vec3i(cx, cy, cz)) == null ? (nullSideChunks ? null : emptyStructure) : structureMap.get(new Vec3i(cx, cy, cz));
    }

    public BlockState getBlockState(VirtualChunk candidateChunk, Vec3i pos, int x, int y, int z) {
        return get(candidateChunk, pos, x, y, z, VirtualChunk::getBlockState);
    }

    public <T> T get(VirtualChunk candidateChunk, Vec3i pos, int x, int y, int z, BlockPositionFunction<T> function) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        VirtualChunk c;
        if (candidateChunk != null && pos.x() == cx && pos.y() == cy && pos.z() == cz) {
            c = candidateChunk;
        } else {
            c = getChunkAtBlock(new Vec3i(x, y, z));
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

    public BlockState getBlockState(Pair<Vec3i, VirtualChunk> a, Pair<Vec3i, VirtualChunk> b, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        VirtualChunk c;
        if (a != null && a.getLeft().x() == cx && a.getLeft().y() == cy && a.getLeft().z() == cz) {
            c = a.getRight();
        } else if (b != null && b.getLeft().x() == cx && b.getLeft().y() == cy && b.getLeft().z() == cz) {
            c = b.getRight();
        } else {
            c = getChunkAtBlock(new Vec3i(x, y, z));
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

    public BlockState getBlockstateAt(Vec3i pos) {
        int cx = Math.floorDiv(pos.x(), 16);
        int cy = Math.floorDiv(pos.y(), 16);
        int cz = Math.floorDiv(pos.z(), 16);

        VirtualChunk c = getChunkAtChunkCoords(new Vec3i(cx, cy, cz));
        if (c == null) return null;

        return c.getBlockState(
        pos.x() - (16 * cx),
        pos.y() - (16 * cy),
        pos.z() - (16 * cz)
        );
    }

    public VirtualChunk putChunkAt(VirtualChunk structure) {
        if (structure != null) {
            structureMap.put(structure.chunkPos, structure);
            recalculateBounds();
        }
        return structure;
    }

    public VirtualChunk getChunkAtChunkCoords(Vec3i pos) {
        return structureMap.get(pos);
    }

    public VirtualChunk getChunkAtChunkCoords(int x, int y, int z) {
        return structureMap.get(new Vec3i(x, y, z));
    }

    public VirtualChunk removeChunkAt(Vec3i pos) {
        VirtualChunk structure = structureMap.get(pos);
        structureMap.remove(pos);
        return structure;
    }

    public void forEachChunk(Consumer<Vec3i> chunkConsumer) {
        for (Vec3i pos : structureMap.keySet()) {
            chunkConsumer.accept(pos);
        }
    }

    public void forEachChunk(BiConsumer<Vec3i, VirtualChunk> chunkConsumer) {
        for (Vec3i pos : structureMap.keySet()) {
            VirtualChunk structure = getChunkAtChunkCoords(pos);

            chunkConsumer.accept(pos, structure);
        }
    }

    public void recalculateBounds() {
        int max_x = 0, max_y = 0, max_z = 0;
        int min_x = 0, min_y = 0, min_z = 0;

        if (structureMap == null) { return; }
        for (Vec3i pos : structureMap.keySet()) {
            max_x = Math.max((16 * (pos.x() + 1)), max_x);
            max_y = Math.max((16 * (pos.y() + 1)), max_y);
            max_z = Math.max((16 * (pos.z() + 1)), max_z);

            min_x = Math.min((16 * pos.x()), min_x);
            min_y = Math.min((16 * pos.y()), min_y);
            min_z = Math.min((16 * pos.z()), min_z);
        }

        AABB.min.set(new Vector3(min_x, min_y, min_z));
        AABB.max.set(new Vector3(max_x, max_y, max_z));
    }

    public void rebuildCollisionShape() {
        forEachChunk((pos, chunk) -> {
            CollisionMeshUtil.createPhysicsMesh(CCS, pos, chunk);
        });
    }

    public int getBlockLight(VirtualChunk chunk, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        VirtualChunk c;
        if (chunk != null && chunk.chunkPos.x() == cx && chunk.chunkPos.y() == cy && chunk.chunkPos.z() == cz) {
            c = chunk;
        } else {
            c = this.getChunkAtBlock(x, y, z);
        }

        if (c == null) {
            return 0;
        } else {
            x -= 16 * cx;
            y -= 16 * cy;
            z -= 16 * cz;
            return c.getBlockLight(x, y, z);
        }
    }

    public void propagateLight() {
        forEachChunk((_v, c) -> {
            c.propagateLights();
        });
    }
}
