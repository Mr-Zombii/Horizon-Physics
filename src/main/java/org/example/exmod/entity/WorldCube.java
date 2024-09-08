package org.example.exmod.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import org.example.exmod.Constants;
import org.example.exmod.boundingBox.OrientedBoundingBoxGetter;
import org.example.exmod.mesh.MutliBlockMesh;
import org.example.exmod.structures.Structure;
import org.example.exmod.util.MatrixUtil;

import java.util.HashMap;
import java.util.Map;

// /summon funni-blocks:entity

public class WorldCube extends Entity {

    public Map<Vec3i, Structure> chunks;

    static SimplexNoise noise = new SimplexNoise(345324532);

    public Vector3 center = new Vector3();
    public Vector3 rotation = new Vector3(0, 0, 0);
    public Matrix4 transform = new Matrix4();

    public void generateChunk(Structure structure, Vec3i vec3i) {
        BlockState air = BlockState.getInstance("base:air[default]");
        BlockState stoneBlock = BlockState.getInstance("base:stone_basalt[default]");
        BlockState waterBlock = BlockState.getInstance("base:water[default]");

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
        for (int x = -2; x < 2; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = -2; z < 2; z++) {
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

    @Override
    public void render(Camera worldCamera) {
        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin2(oBoundingBox, tmpModelMatrix, position, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix);
            }
        }
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        center = new Vector3(position.x != 0 ? position.x / 2 : 0, position.y != 0 ? position.y / 2 : 0, position.z != 0 ? position.z / 2 : 0);

        MatrixUtil.rotateAroundOrigin2(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(rBoundingBox);
        oBoundingBox.setTransform(transform);

        if (!((OrientedBoundingBoxGetter)localBoundingBox).hasInnerBounds()) {
            setBounds();
            ((OrientedBoundingBoxGetter)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
        rotation.x += 1f;
        super.updateEntityChunk(zone);
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
        rBoundingBox.max.set(new Vector3(max_x, max_y, max_z));
        rBoundingBox.min.set(new Vector3(min_x, min_y, min_z));
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((OrientedBoundingBoxGetter) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }
}
