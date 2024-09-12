package org.example.exmod.util;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.util.Vec3i;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
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
//                    BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
//                    mesh.addChildShape(boxShape, new Vector3f(globalX, globalY, globalZ));
                    shapeFromBlockState(mesh, new Vector3f(globalX, globalY, globalZ), state);
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
//                    BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
//                    mesh.addChildShape(boxShape, new Vector3f(pos.x, pos.y, pos.z));
                    shapeFromBlockState(mesh, new Vector3f(x, y, z), state);
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

    public static void shapeFromBlockState(CompoundCollisionShape mesh, Vector3f vector3f, BlockState state){
        Array<BoundingBox> boundingBoxes = new Array<>();
        state.getAllBoundingBoxes(boundingBoxes, 0, 0, 0);

        if (boundingBoxes.size == 1) {
            mesh.addChildShape(shapeFromAABB(boundingBoxes.get(0)), vector3f);
            return;
        }
        while (!boundingBoxes.isEmpty()) {
            BoundingBox box = boundingBoxes.pop();
            com.jme3.bounding.BoundingBox boundingBox = minMaxToExtents(box.min, box.max);
            BoxCollisionShape shape = shapeFromAABB(box);

            mesh.addChildShape(shape, vector3f.add(new Vector3f(box.min.x, box.min.y, box.min.z)).subtract(0.5f, 0.5f, 0.5f).add(boundingBox.getExtent(new Vector3f())));
        }
    }

    public static com.jme3.bounding.BoundingBox minMaxToExtents(Vector3 min, Vector3 max) {
        return new com.jme3.bounding.BoundingBox(new Vector3f(min.x, min.y, min.z), new Vector3f(max.x, max.y, max.z));
    }

    public static BoxCollisionShape shapeFromAABB(BoundingBox bb) {
        com.jme3.bounding.BoundingBox extents = minMaxToExtents(bb.min, bb.max);

        return new BoxCollisionShape(extents.getExtent(new Vector3f()));
    }

    public static boolean isCollideableState(BlockState state) {
        return !(state == null || state.walkThrough);
    }

}
