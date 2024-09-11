package org.example.exmod.util;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.util.Vec3i;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Chunk;
import org.example.exmod.world.Structure;

public class CollisionMeshUtil {

    public static CompoundCollisionShape createPhysicsMesh(CompoundCollisionShape mesh, Vec3i pos, Structure chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int globalX = (pos.x() * 16) + x;
                    int globalY = (pos.y() * 16) + y;
                    int globalZ = (pos.z() * 16) + z;

                    BlockState state = chunk.getBlockState(x, y, z);
                    if (!isCollideableState(state)) { continue; }
                    Vector3 pos2 = new Vector3(x, y, z);
                    BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
                    mesh.addChildShape(boxShape, new Vector3f(globalX, globalY, globalZ));
//                    CollisionShape blockCollisionShape = shapeFromBlockState(state);
//                    mesh.addChildShape(blockCollisionShape, new Vector3f(globalX, y, globalZ));
                }
            }
        }

        return mesh;
    }

    public static CompoundCollisionShape createPhysicsMesh(CompoundCollisionShape mesh, Chunk chunk) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int globalX = chunk.blockX + x;
                    int globalY = chunk.blockY + y;
                    int globalZ = chunk.blockZ + z;

                    BlockState state = chunk.getBlockState(x, y, z);
                    if (!isCollideableState(state)) continue;
                    Vector3 pos = new Vector3(x, y, z);
                    BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
                    mesh.addChildShape(boxShape, new Vector3f(pos.x, pos.y, pos.z));
//                    CollisionShape blockCollisionShape = shapeFromBlockState(state);
//                    mesh.addChildShape(blockCollisionShape, new Vector3f(x, y, z));
                }
            }
        }

        return mesh;
    }

    public static CompoundCollisionShape createPhysicsMesh(Vec3i pos, Structure chunk) {
        return createPhysicsMesh(new CompoundCollisionShape(), pos, chunk);
    }

    public static CompoundCollisionShape createPhysicsMesh(Chunk chunk) {
        return createPhysicsMesh(new CompoundCollisionShape(), chunk);
    }

    public static CollisionShape shapeFromBlockState(BlockState state){
        CompoundCollisionShape collisionShape = new CompoundCollisionShape();

        Array<BoundingBox> boundingBoxes = new Array<>();
        state.getAllBoundingBoxes(boundingBoxes, 0, 0, 0);

        if (boundingBoxes.size == 1) return shapeFromAABB(boundingBoxes.get(0));
        while (!boundingBoxes.isEmpty()) {
            BoundingBox box = boundingBoxes.pop();

            collisionShape.addChildShape(shapeFromAABB(box), new Vector3f(0, 0, 0));
        }

        return collisionShape;
    }

    public static Vector3f minMaxToExtents(Vector3 min, Vector3 max) {
        Vector3f extents = new Vector3f();
        extents.x = (max.x - min.x) / 2;
        extents.y = (max.y - min.y) / 2;
        extents.z = (max.z - min.z) / 2;
        return extents;
    }

    public static BoxCollisionShape shapeFromAABB(BoundingBox bb) {
        Vector3f extents = minMaxToExtents(bb.min, bb.max);

        return new BoxCollisionShape(extents);
    }

    public static boolean isCollideableState(BlockState state) {
        return !(state == null || state.walkThrough);
    }

}