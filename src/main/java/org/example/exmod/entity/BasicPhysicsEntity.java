package org.example.exmod.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.game.util.Reflection;
import com.github.puzzle.util.Vec3i;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.example.exmod.Constants;
import org.example.exmod.bounds.ExtendedBoundingBox;
import org.example.exmod.mesh.MutliBlockMesh;
import org.example.exmod.threading.PhysicsThread;
import org.example.exmod.util.MatrixUtil;
import org.example.exmod.world.VirtualChunk;
import org.example.exmod.world.VirtualWorld;

import java.util.UUID;

public class BasicPhysicsEntity extends Entity implements IPhysicEntity {

    public PhysicsRigidBody body;

    public Quaternion rotation;
    UUID uuid;
    float mass = 5;

    public VirtualWorld world;

    public Matrix4 transform = new Matrix4();

    public BasicPhysicsEntity() {
        super(Constants.MOD_ID + ":physics_entity");

        if (rotation == null) rotation = new Quaternion(0, 0, 0, 0);
        if (uuid == null) uuid = UUID.randomUUID();

        world = new VirtualWorld();
        VirtualChunk structure00 = new VirtualChunk((short) 0, new Vec3i(0, 0, 0), new Identifier("e", "E"));
        structure00.setParentWorld(world);
        VirtualChunk structure01 = new VirtualChunk((short) 0, new Vec3i(-1, 0, 0), new Identifier("e", "E"));
        structure01.setParentWorld(world);
        VirtualChunk structure10 = new VirtualChunk((short) 0, new Vec3i(0, 0, -1), new Identifier("e", "E"));
        structure10.setParentWorld(world);
        VirtualChunk structure11 = new VirtualChunk((short) 0, new Vec3i(-1, 0, -1), new Identifier("e", "E"));
        structure11.setParentWorld(world);

        BlockState stone = BlockState.getInstance("base:stone_basalt[default]");
        BlockState light = BlockState.getInstance("base:light[power=on,lightRed=15,lightGreen=0,lightBlue=0]");
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                structure00.setBlockState(stone, x, 0, z);
                structure01.setBlockState(stone, x + 8, 0, z);
                structure10.setBlockState(stone, x, 0, z + 8);
                structure11.setBlockState(stone, x + 8, 0, z + 8);
            }
        }
        BlockState chair = BlockState.getInstance(Constants.MOD_ID + ":chair[default]");
        structure00.setBlockState(light, 0, 1, 0);
        structure01.setBlockState(light, 15, 1, 0);
        structure10.setBlockState(light, 0, 1, 15);
        structure11.setBlockState(light, 15, 1, 15);
//        structure00.setBlockState(chair, 0, 1, 0);
//        structure01.setBlockState(chair, 15, 1, 0);
//        structure10.setBlockState(chair, 0, 1, 15);
//        structure11.setBlockState(chair, 15, 1, 15);
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
        body.activate(true);
        PerspectiveCamera cam = Reflection.getFieldContents(GameState.IN_GAME, "rawWorldCamera");
        body.setLinearVelocity(new Vector3f(cam.direction.cpy().scl(12).x, cam.direction.cpy().scl(12).y, cam.direction.cpy().scl(12).z));
    }

    @Override
    public void update(Zone zone, double deltaTime) {
        if (!PhysicsThread.INSTANCE.shouldRun) return;
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(world.AABB);
        oBoundingBox.setTransform(transform);

        if (!hasInit) {
            PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setMass(5);
            hasInit = true;


            PhysicsThread.addEntity(this);
            body.activate(true);
        } else {
            Vector3f vector3f = body.getPhysicsLocation(new Vector3f());
            position.set(vector3f.x, vector3f.y, vector3f.z);
            rotation = body.getPhysicsRotation(new Quaternion());
//            System.out.println(vector3f);
        }

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
//        rotation.x += 1;
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
//        DebugRenderUtil.renderRigidBody(((InGameAccess) InGame.IN_GAME).getShapeRenderer(), position, body);
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
}
