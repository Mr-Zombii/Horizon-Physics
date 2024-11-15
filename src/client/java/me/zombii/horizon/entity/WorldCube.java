package me.zombii.horizon.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualWorldEntity;
import me.zombii.horizon.util.Vec3i;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.noise.SimplexNoise;
import org.checkerframework.checker.nullness.qual.NonNull;
import me.zombii.horizon.Constants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.mesh.MutliBlockMesh;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.world.VirtualChunk;
import me.zombii.horizon.world.VirtualWorld;
import me.zombii.horizon.util.MatrixUtil;

import java.util.UUID;
import java.util.function.Supplier;

// /summon funni-blocks:entity

public class WorldCube extends Entity implements IPhysicEntity, IVirtualWorldEntity {

    public VirtualWorld world;
    public PhysicsRigidBody rigidBody;

    static SimplexNoise noise = new SimplexNoise(345324532);

    public Vector3 rotation = new Vector3(0, 0, 0);
    public Matrix4 transform = new Matrix4();

    public UUID uuid;

    public void generateChunk(VirtualChunk structure, Vec3i vec3i) {
        BlockState air = BlockState.getInstance("base:air[default]");
        BlockState stoneBlock = BlockState.getInstance("base:stone_basalt[default]");
//        BlockState waterBlock = BlockState.getInstance("base:water[default]");
        BlockState waterBlock = BlockState.getInstance("base:light[power=on,lightRed=0,lightGreen=15,lightBlue=0]");

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

    public WorldCube(VirtualWorld world) {
        super(Identifier.of(Constants.MOD_ID, "entity").toString());
        this.world = world;
        uuid = UUID.randomUUID();
        world.rebuildCollisionShape();
        rigidBody = new PhysicsRigidBody(world.CCS);

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(world));
        hasGravity = false;
        transform.idt();
    }

    public WorldCube() {
        super(Constants.MOD_ID + ":rotating_entity");

        if (uuid == null)
            uuid = UUID.randomUUID();

        world = new VirtualWorld();
        for (int x = -2; x < 2; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = -2; z < 2; z++) {
                    Vec3i vec3i = new Vec3i(x, y, z);

                    VirtualChunk structure = new VirtualChunk(
                            (short) 0,
                            new Vec3i(x, y, z)
                    );
                    structure.setParentWorld(world);
                    generateChunk(structure, vec3i);
                    world.putChunkAt(structure);
                }
            }
        }
        world.rebuildCollisionShape();

        rigidBody = new PhysicsRigidBody(world.CCS);

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(world));

        hasGravity = false;
    }

    @Override
    public void render(Camera worldCamera) {
//        DebugRenderUtil.renderRigidBody(((InGameAccess) InGame.IN_GAME).getShapeRenderer(), rigidBody);

        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin2(oBoundingBox, tmpModelMatrix, tmpRenderPos, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix);
            }
        }
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
        MatrixUtil.rotateAroundOrigin2(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
            rigidBody.setPhysicsRotation(getEularRotation());
            rigidBody.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            rigidBody.setMass(0);
            PhysicsThread.addEntity(this);
        }

        if (world.CCS_WAS_REBUILT) {
            System.out.println("rebuilding");
            PhysicsThread.INSTANCE.space.getRigidBodyList().forEach(e -> {
                if (e.nativeId() == rigidBody.nativeId()) {
                    System.out.println("E");
                    e.setCollisionShape(world.CCS);
                }
            });
            rigidBody.setCollisionShape(world.CCS);
            world.CCS_WAS_REBUILT = false;
            System.out.println("rebuilt");
        }

        getBoundingBox(globalBoundingBox);
        rigidBody.setPhysicsRotation(getEularRotation());
        rotation.x = 0;
        rotation.y = 0;
        rotation.z = 0;
        super.updateEntityChunk(zone);
    }

    @Override
    public @NonNull PhysicsBody getBody() {
        return rigidBody;
    }

    @Override
    public Quaternion getEularRotation() {
        com.badlogic.gdx.math.Quaternion quaternion = transform.getRotation(new com.badlogic.gdx.math.Quaternion());
        return new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    @Override
    public @NonNull UUID getUUID() {
        return uuid;
    }

    @Override
    public float getMass() {
        return 0;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return world.CCS;
    }

    @Override
    public void setEularRotation(Quaternion rot) {

    }

    @Override
    public void setUUID(UUID uuid) {

    }

    @Override
    public void setMass(float mass) {

    }

    @Override
    public void setCollisionShape(CollisionShape shape) {

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

    @Override
    public VirtualWorld getWorld() {
        return world;
    }
}
