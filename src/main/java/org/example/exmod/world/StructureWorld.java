package org.example.exmod.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.util.Vec3i;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.math.Vector3f;
import com.llamalad7.mixinextras.lib.apache.commons.tuple.Pair;
import finalforeach.cosmicreach.blocks.BlockState;
import org.example.exmod.Constants;
import org.example.exmod.mesh.BlockPositionFunction;
import org.example.exmod.util.CollisionMeshUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StructureWorld {

    Map<Vec3i, Structure> structureMap = new HashMap<>();
    public BoundingBox AABB = new BoundingBox();
    public CompoundCollisionShape CCS = new CompoundCollisionShape();

    public StructureWorld() {}

    public boolean hasCollisionShape = false;
    public boolean nullSideChunks = false;

    private static final Structure emptyStructure = new Structure((short) 0, new Identifier(Constants.MOD_ID, "empty"));

    public Structure getStructureBlock(Vec3i blockPos) {
        int cx = Math.floorDiv(blockPos.x(), 16);
        int cy = Math.floorDiv(blockPos.y(), 16);
        int cz = Math.floorDiv(blockPos.z(), 16);

        return structureMap.get(new Vec3i(cx, cy, cz)) == null ? (nullSideChunks ? null : emptyStructure) : structureMap.get(new Vec3i(cx, cy, cz));
    }

    public BlockState getBlockState(Structure candidateChunk, Vec3i pos, int x, int y, int z) {
        return get(candidateChunk, pos, x, y, z, Structure::getBlockState);
    }

    public <T> T get(Structure candidateChunk, Vec3i pos, int x, int y, int z, BlockPositionFunction<T> function) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Structure c;
        if (candidateChunk != null && pos.x() == cx && pos.y() == cy && pos.z() == cz) {
            c = candidateChunk;
        } else {
            c = getStructureBlock(new Vec3i(x, y, z));
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

    public BlockState getBlockState(Pair<Vec3i, Structure> a, Pair<Vec3i, Structure> b, int x, int y, int z) {
        int cx = Math.floorDiv(x, 16);
        int cy = Math.floorDiv(y, 16);
        int cz = Math.floorDiv(z, 16);
        Structure c;
        if (a != null && a.getLeft().x() == cx && a.getLeft().y() == cy && a.getLeft().z() == cz) {
            c = a.getRight();
        } else if (b != null && b.getLeft().x() == cx && b.getLeft().y() == cy && b.getLeft().z() == cz) {
            c = b.getRight();
        } else {
            c = getStructureBlock(new Vec3i(x, y, z));
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

        Structure c = getChunkAt(new Vec3i(cx, cy, cz));
        if (c == null) return null;

        return c.getBlockState(
        pos.x() - (16 * cx),
        pos.y() - (16 * cy),
        pos.z() - (16 * cz)
        );
    }

    public Structure putChunkAt(Vec3i pos, Structure structure) {
        if (structure != null) {
            structureMap.put(pos, structure);
            recalculateBounds();
        }
        return structure;
    }

    public Structure getChunkAt(Vec3i pos) {
        return structureMap.get(pos);
    }

    public Structure removeChunkAt(Vec3i pos) {
        Structure structure = structureMap.get(pos);
        structureMap.remove(pos);
        return structure;
    }

    public void forEachChunk(Consumer<Vec3i> chunkConsumer) {
        for (Vec3i pos : structureMap.keySet()) {
            chunkConsumer.accept(pos);
        }
    }

    public void forEachChunk(BiConsumer<Vec3i, Structure> chunkConsumer) {
        for (Vec3i pos : structureMap.keySet()) {
            Structure structure = getChunkAt(pos);

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

}
