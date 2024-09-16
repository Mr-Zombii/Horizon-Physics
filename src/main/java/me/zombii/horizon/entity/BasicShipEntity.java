package me.zombii.horizon.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
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
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import org.checkerframework.checker.nullness.qual.NonNull;
import me.zombii.horizon.Constants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.mesh.MutliBlockMesh;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.MatrixUtil;
import me.zombii.horizon.world.VirtualChunk;
import me.zombii.horizon.world.VirtualWorld;

import java.util.UUID;

public class BasicShipEntity extends Entity implements IPhysicEntity, IVirtualWorldEntity {

    public PhysicsRigidBody body;

    public Quaternion rotation;
    UUID uuid;
    float mass = 5;

    public VirtualWorld world;

    public Matrix4 transform = new Matrix4();

    public BasicShipEntity() {
        super(Constants.MOD_ID + ":ship");

        if (rotation == null) rotation = new Quaternion(0, 0, 0, 0);
        if (uuid == null) uuid = UUID.randomUUID();

        world = new VirtualWorld();
        VirtualChunk structure00 = new VirtualChunk((short) 0, new Vec3i(0, 0, 0));
        VirtualChunk structure01 = new VirtualChunk((short) 0, new Vec3i(-1, 0, 0));
        VirtualChunk structure10 = new VirtualChunk((short) 0, new Vec3i(0, 0, -1));
        VirtualChunk structure11 = new VirtualChunk((short) 0, new Vec3i(-1, 0, -1));

        BlockState stone = BlockState.getInstance("base:stone_basalt[default]");
        BlockState chair = BlockState.getInstance(Constants.MOD_ID + ":chair[default]");
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                structure00.setBlockState(stone, x, 0, z);
                structure01.setBlockState(stone, x + 8, 0, z);
                structure10.setBlockState(stone, x, 0, z + 8);
                structure11.setBlockState(stone, x + 8, 0, z + 8);
            }
        }
//        structure00.setBlockState(light, 0, 1, 0);
//        structure01.setBlockState(light, 15, 1, 0);
//        structure10.setBlockState(light, 0, 1, 15);
//        structure11.setBlockState(light, 15, 1, 15);
//        structure00.setBlockState(BlockState.getInstance("base:lunar_soil_packed[stair_type=bottom_PosZ,default]"), 0, 1, 0);
//        structure01.setBlockState(BlockState.getInstance("base:lunar_soil_packed[stair_type=bottom_PosX,default]"), 15, 1, 0);
//        structure10.setBlockState(BlockState.getInstance("base:lunar_soil_packed[stair_type=bottom_NegX,default]"), 0, 1, 15);
//        structure11.setBlockState(BlockState.getInstance("base:lunar_soil_packed[stair_type=bottom_NegZ,default]"), 15, 1, 15);

        structure00.setBlockState(chair, 0, 1, 0);
        structure01.setBlockState(chair, 15, 1, 0);
        structure10.setBlockState(chair, 0, 1, 15);
        structure11.setBlockState(chair, 15, 1, 15);
        world.putChunkAt(structure00);
        world.putChunkAt(structure01);
        world.putChunkAt(structure10);
        world.putChunkAt(structure11);
        world.rebuildCollisionShape();

        body = new PhysicsRigidBody(world.CCS);

        Threads.runOnMainThread(() -> modelInstance = new MutliBlockMesh(world));

        hasGravity = false;
        transform.idt();
    }

    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    boolean hasInit = false;

    @Override
    protected void onDeath(Zone zone) {
        PhysicsThread.removeEntity(this);
        super.onDeath(zone);
    }

    @Override
    public void hit(float amount) {
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        if (!hasInit) {
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setMass(0);
            PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
            hasInit = true;


            PhysicsThread.addEntity(this);
            body.activate(true);
        } else {
            Vector3f vector3f = body.getPhysicsLocation(new Vector3f());
            vector3f = vector3f.subtract(0.5f, 0.5f, 0.5f);
            position.set(vector3f.x, vector3f.y, vector3f.z);
            rotation = body.getPhysicsRotation(new Quaternion());
        }

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        if (world.CCS_WAS_REBUILT) {
            body.setCollisionShape(world.CCS);
            world.CCS_WAS_REBUILT = false;
        }

        getBoundingBox(globalBoundingBox);
        super.updateEntityChunk(zone);
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((ExtendedBoundingBox) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        uuid = IPhysicEntity.readOrDefault(() -> {
            String uid = deserial.readString("uuid");
            return UUID.fromString(uid);
        }, UUID.randomUUID());

        rotation = IPhysicEntity.readOrDefault(() -> {
            float rot_x = deserial.readFloat("rot_x", 0);
            float rot_y = deserial.readFloat("rot_y", 0);
            float rot_z = deserial.readFloat("rot_z", 0);
            float rot_w = deserial.readFloat("rot_w", 0);

            return new Quaternion(rot_x, rot_y, rot_z, rot_w);
        }, new Quaternion(0, 0, 0, 0));

        body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
        body.setPhysicsRotation(rotation);
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        serial.writeString("uuid", uuid == null ? UUID.randomUUID().toString() : uuid.toString());

        serial.writeFloat("rot_x", rotation.getX());
        serial.writeFloat("rot_y", rotation.getY());
        serial.writeFloat("rot_z", rotation.getZ());
        serial.writeFloat("rot_w", rotation.getW());
    }

    @Override
    public void render(Camera worldCamera) {
//        DebugRenderUtil.renderRigidBody(((InGameAccess)InGame.IN_GAME).getShapeRenderer(), body);

        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin3(oBoundingBox, tmpModelMatrix, tmpRenderPos, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix);
            }
        }
//        DebugRenderUtil.renderRigidBody(((InGameAccess) InGame.IN_GAME).getShapeRenderer(), body);
    }

    @Override
    @NonNull
    public PhysicsBody getBody() {
        return body;
    }

    @Override
    public Quaternion getEularRotation() {
        return rotation;
    }

    @Override
    @NonNull
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return world.CCS;
    }

    @Override
    public void setEularRotation(Quaternion rot) {
        this.rotation = rot;
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setMass(float mass) {

    }

    @Override
    public void setCollisionShape(CollisionShape shape) {

    }

    @Override
    public VirtualWorld getWorld() {
        return world;
    }
}
