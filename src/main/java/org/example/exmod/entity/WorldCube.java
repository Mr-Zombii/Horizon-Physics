package org.example.exmod.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.util.Vec3i;
import com.jme3.bullet.objects.PhysicsRigidBody;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import org.example.exmod.Constants;
import org.example.exmod.boundingBox.ExtendedBoundingBox;
import org.example.exmod.mesh.MutliBlockMesh;
import org.example.exmod.world.Structure;
import org.example.exmod.world.StructureWorld;
import org.example.exmod.util.MatrixUtil;

import java.util.UUID;
import java.util.function.Supplier;

// /summon funni-blocks:entity

public class WorldCube extends Entity {

    public StructureWorld world;
    public PhysicsRigidBody rigidBody;

    static SimplexNoise noise = new SimplexNoise(345324532);

    public Vector3 rotation = new Vector3(0, 0, 0);
    public Matrix4 transform = new Matrix4();

    public UUID uuid;

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

    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    public WorldCube(StructureWorld world) {
        super(new Identifier(Constants.MOD_ID, "entity").toString());
        this.world = world;
        uuid = UUID.randomUUID();
        world.rebuildCollisionShape();
        rigidBody = new PhysicsRigidBody(world.CCS);

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(world));
        hasGravity = false;
        transform.idt();
    }

    public WorldCube() {
        super(new Identifier(Constants.MOD_ID, "entity").toString());

        if (uuid == null)
            uuid = UUID.randomUUID();

        world = new StructureWorld();
        for (int x = -2; x < 2; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = -2; z < 2; z++) {
                    Vec3i vec3i = new Vec3i(x, y, z);

                    Structure structure = new Structure(
                            (short) 0,
                            new Identifier("base", "test")
                    );
                    generateChunk(structure, vec3i);
                    world.putChunkAt(vec3i, structure);
                }
            }
        }

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(world));

        hasGravity = false;
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
        MatrixUtil.rotateAroundOrigin2(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
        rotation.x += 1f;
        rotation.y += 1f;
        rotation.z += 1f;
        super.updateEntityChunk(zone);
    }

    <T> T readOrDefault(Supplier<T> read, T _default) {
        try {
            return read.get();
        } catch (Exception ignore) {
            return _default;
        }
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        uuid = readOrDefault(() -> {
            String uid = deserial.readString("uuid");
            return UUID.fromString(uid);
        }, UUID.randomUUID());

        rotation = readOrDefault(() -> {
            float yaw = deserial.readFloat("yaw", rotation.x);
            float pitch = deserial.readFloat("pitch", rotation.y);
            float roll = deserial.readFloat("roll", rotation.z);

            return new Vector3(yaw, pitch, roll);
        }, new Vector3(0, 0, 0));
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        serial.writeString("uuid", uuid == null ? UUID.randomUUID().toString() : uuid.toString());

        serial.writeFloat("yaw", rotation.x);
        serial.writeFloat("pitch", rotation.y);
        serial.writeFloat("roll", rotation.z);
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((ExtendedBoundingBox) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }
}
