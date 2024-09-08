package org.example.exmod.entity;

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
import org.example.exmod.mesh.MutliBlockMesh;
import org.example.exmod.structures.Structure;

import java.util.HashMap;
import java.util.Map;

// /summon funni-blocks:entity

public class WorldCube extends Entity {

    public Map<Vec3i, Structure> chunks;

    static SimplexNoise noise = new SimplexNoise(345324532);

    public Vector3 rotation = new Vector3(0, 0, 0);

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

    public OrientedBoundingBox entityBoundingBox;

    public WorldCube(Map<Vec3i, Structure> structureMap) {
        super(new Identifier(Constants.MOD_ID, "entity").toString());
        chunks = structureMap;

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(this));
        hasGravity = false;

        entityBoundingBox = new OrientedBoundingBox();
        localBoundingBox = entityBoundingBox.getBounds();
    }

    public WorldCube() {
        super(new Identifier(Constants.MOD_ID, "entity").toString());
        chunks = new HashMap<>();
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = 0; z < 32; z++) {
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

        entityBoundingBox = new OrientedBoundingBox();
        localBoundingBox = entityBoundingBox.getBounds();
        hasGravity = true;

    }

    @Override
    public void update(Zone zone, double deltaTime) {
//        this.localBoundingBox.min.add(this.position);
//        this.localBoundingBox.max.add(this.position);
        localBoundingBox = entityBoundingBox.getBounds();
//        this.localBoundingBox.update();

        getBoundingBox(globalBoundingBox);
        super.updateEntityChunk(zone);
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
        localBoundingBox.min.set(position);
        localBoundingBox.max.set(new Vector3(400, 100, 400).add(position));
        boundingBox.set(localBoundingBox);
        boundingBox.update();
    }
}
