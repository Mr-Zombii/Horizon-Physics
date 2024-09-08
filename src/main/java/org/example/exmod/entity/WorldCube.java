package org.example.exmod.entity;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import org.example.exmod.Constants;
import org.example.exmod.boundingBox.OrientedBoundingBoxGetter;
import org.example.exmod.mesh.MutliBlockMesh;
import org.example.exmod.structures.Structure;

import java.util.HashMap;
import java.util.Map;

// /summon funni-blocks:entity

public class WorldCube extends Entity {

    public Map<Vec3i, Structure> chunks;

    static SimplexNoise noise = new SimplexNoise(345324532);

    public Vector3 rotation = new Vector3(0, 0, 0);
    public Matrix4 transform = new Matrix4();
    public float[] transform2 = new float[]{
            (float) Math.cos(1),0,(float) Math.sin(1),0,
            0,1,0,0,
            -(float) Math.sin(1),0,(float) Math.cos(1),0,
            0,0,0,1
    };

    public void generateChunk(Structure structure, Vec3i vec3i) {
        BlockState air = BlockState.getInstance("base:air[default]");
        BlockState stoneBlock = BlockState.getInstance("base:stone_basalt[default]");
        BlockState waterBlock = BlockState.getInstance("base:water[default]");
//        BatchedZoneRenderer;
//        ExperimentalNaiveZoneRenderer

        for(int localX = 0; localX < 16; localX++) {
            int globalX = (vec3i.x() * 16) + localX;

            for(int localZ = 0; localZ < 16; localZ++) {
                int globalZ = (vec3i.z() * 16) + localZ;

                // Only have to sample height once for each Y column (not going to change on the Y axis ;p)
                double columnHeight = noise.noise2(globalX * 0.01f, globalZ * 0.01f) * 32f + 64f;

                for (int localY = 0; localY < 16; localY++) {
                    int globalY = (vec3i.y() * 16) + localY;

                    if(globalY <= columnHeight) {
                        structure.setBlockState(stoneBlock, localX, localY, localZ);
                    } else {
                        if (globalY <= 64) {
                            if (structure.getBlockState(localX, localY, localZ) == air) {
                                structure.setBlockState(waterBlock, localX, localY, localZ);
                            }
                        }
                    }

                }
            }
        }
    }

    public BoundingBox rBoundingBox = new BoundingBox();
    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    public WorldCube(Map<Vec3i, Structure> structureMap) {
        super(new Identifier(Constants.MOD_ID, "entity").toString());
        chunks = structureMap;

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(this));
        hasGravity = false;
        transform.idt();
    }

    public WorldCube() {
        super(new Identifier(Constants.MOD_ID, "entity").toString());
        chunks = new HashMap<>();
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = 0; z < 4; z++) {
                    Vec3i vec3i = new Vec3i(x, y, z);

                    Structure structure = new Structure(
                            (short) 0,
                            new Identifier("base", "test")
                    );
                    generateChunk(structure, vec3i);
                    chunks.put(vec3i, structure);
                }
            }
        }

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(this));

        hasGravity = true;
    }

    Quaternion rot = new Quaternion();

    boolean isSet = false;

    @Override
    public void update(Zone zone, double deltaTime) {
//        this.localBoundingBox.min.add(this.position);
//        this.localBoundingBox.max.add(this.position);
//        this.localBoundingBox.update();

        transform.setToRotation(new Vector3(0, 1, 0), 3);
        transform.setTranslation(new Vector3(-position.x, -position.y, -position.z));

//        transform.set(transform2);
//        transform.setToTranslation(position);
//        transform.set(new Vector3(0, 0, 0), rot, new Vector3(1, 1, 1));

//        transform.setToRotation(new Vector3(0, 1, 0), 1);
//        transform.rotate(1, 0, 0, rotation.x);
//        transform.rotate(0, 1, 0, rotation.y);
//        transform.rotate(0, 0, 1, rotation.z);

//        rBoundingBox.min.set(position);
//        rBoundingBox.max.set(new Vector3(400, 100, 400).add(position));
//        rBoundingBox.update();


//        if (!isSet) {
//            setBounds();
//            Matrix3 matrix3 = new Matrix3();
//            matrix3.scale(2, 2);
//            mul(matrix3);
////            rBoundingBox.mul(transform);
////            rBoundingBox.update();
//            localBoundingBox.set(rBoundingBox);
//            localBoundingBox.update();
//            isSet = true;
//        }

        oBoundingBox.setBounds(rBoundingBox);
        oBoundingBox.setTransform(transform);

        if (!((OrientedBoundingBoxGetter)localBoundingBox).hasInnerBounds()) {
            setBounds();
            ((OrientedBoundingBoxGetter)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
        super.updateEntityChunk(zone);
    }

    public void mul(Matrix3 transform) {
        Vector3 tmpVector = new Vector3();

        final float x0 = rBoundingBox.min.x,y0 = rBoundingBox.min.y, z0 = rBoundingBox.min.z, x1 = rBoundingBox.max.x, y1 = rBoundingBox.max.y, z1 = rBoundingBox.max.z;
        ext(tmpVector.set(x0, y0, z0).mul(transform));
        ext(tmpVector.set(x0, y0, z1).mul(transform));
        ext(tmpVector.set(x0, y1, z0).mul(transform));
        ext(tmpVector.set(x0, y1, z1).mul(transform));
        ext(tmpVector.set(x1, y0, z0).mul(transform));
        ext(tmpVector.set(x1, y0, z1).mul(transform));
        ext(tmpVector.set(x1, y1, z0).mul(transform));
        ext(tmpVector.set(x1, y1, z1).mul(transform));
    }

    public void ext(Vector3 point) {
        rBoundingBox.set(rBoundingBox.min.set(Math.min(rBoundingBox.min.x, point.x), Math.min(rBoundingBox.min.y, point.y), Math.min(rBoundingBox.min.z, point.z)),
                rBoundingBox.max.set(Math.max(rBoundingBox.max.x, point.x), Math.max(rBoundingBox.max.y, point.y), Math.max(rBoundingBox.max.z, point.z)));
    }

    public void setBounds() {
        int max_x = 0, max_y = 0, max_z = 0;
        int min_x = 0, min_y = 0, min_z = 0;

        if (chunks == null) { return; }
        for (Vec3i pos : chunks.keySet()) {
            max_x = (16 * (pos.x() + 1)) > max_x ? (16 * (pos.x() + 1)) : max_x;
            max_y = (16 * (pos.y() + 1)) > max_y ? (16 * (pos.y() + 1)) : max_y;
            max_z = (16 * (pos.z() + 1)) > max_z ? (16 * (pos.z() + 1)) : max_z;

            min_x = (16 * pos.x()) < min_x ? (16 * pos.x()) : min_x;
            min_y = (16 * pos.y()) < min_y ? (16 * pos.y()) : min_y;
            min_z = (16 * pos.z()) < min_z ? (16 * pos.z()) : min_z;
        }
        rBoundingBox.max.set(new Vector3(max_x, max_y, max_z).add(position));
        rBoundingBox.min.set(new Vector3(min_x, min_y, min_z).add(position));
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
//        boundingBox.set(this.localBoundingBox);
//
//        int max_x = 0, max_y = 0, max_z = 0;
//        int min_x = 0, min_y = 0, min_z = 0;
//
//        if (chunks == null) { return; }
//        for (Vec3i pos : chunks.keySet()) {
//            max_x = (16 * (pos.x() + 1)) > max_x ? (16 * (pos.x() + 1)) : max_x;
//            max_y = (16 * (pos.y() + 1)) > max_y ? (16 * (pos.y() + 1)) : max_y;
//            max_z = (16 * (pos.z() + 1)) > max_z ? (16 * (pos.z() + 1)) : max_z;
//
//            min_x = (16 * pos.x()) < min_x ? (16 * pos.x()) : min_x;
//            min_y = (16 * pos.y()) < min_y ? (16 * pos.y()) : min_y;
//            min_z = (16 * pos.z()) < min_z ? (16 * pos.z()) : min_z;
//        }
//        localBoundingBox.max.set(new Vector3(max_x, max_y, max_z).add(position));
//        localBoundingBox.min.set(new Vector3(min_x, min_y, min_z).add(position));
//        boundingBox.set(localBoundingBox);
//        boundingBox.update();

//        ((OrientedBoundingBoxGetter) boundingBox).setInnerBounds(oBoundingBox);
        if (transform != null) {
            OrientedBoundingBox box = new OrientedBoundingBox();
            Matrix4 matrix4 = transform.cpy().setTranslation(Vector3.Zero);
            box.set(rBoundingBox, matrix4);
            ((OrientedBoundingBoxGetter) localBoundingBox).setInnerBounds(box);
            boundingBox.set(localBoundingBox);
            boundingBox.update();
        }
    }
}
