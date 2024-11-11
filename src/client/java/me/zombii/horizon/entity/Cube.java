package me.zombii.horizon.entity;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.Constants;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.ISingleEntityBlock;
import me.zombii.horizon.items.GravityGun;
import me.zombii.horizon.mesh.SingleBlockMesh;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import me.zombii.horizon.util.MatrixUtil;
import me.zombii.horizon.world.physics.ChunkMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Cube extends Entity implements IPhysicEntity, ISingleEntityBlock {

    public PhysicsRigidBody body;
    public Matrix4 transform;
    public Quaternion rotation;
    public UUID uuid;
    public Float mass;
    public CollisionShape shape;
    public AtomicReference<BlockState> state = new AtomicReference<>();
    boolean isPickedUp;

    public BoundingBox rBoundingBox = new BoundingBox(new Vector3(-0.5f, -0.5f,-0.5f), new Vector3(0.5f, 0.5f, 0.5f));
    public OrientedBoundingBox oBoundingBox = new OrientedBoundingBox();

    public Cube() {
        super(Constants.MOD_ID + ":cube");

        state.set(BlockState.getInstance("base:furnace[lit=off]"));
        shape = PhysicsThread.INSTANCE.shapeFromBlockState(new CompoundCollisionShape(), new Vector3f(), state.get());

        body = new PhysicsRigidBody(shape);
        body.setFriction(1f);
        mass = 2.5f;

        uuid = UUID.randomUUID();
        rotation = new Quaternion();
        transform = new Matrix4();

        Threads.runOnMainThread(() -> modelInstance = new SingleBlockMesh(state));
    }

    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
    }

    @Override
    public boolean isPickedUp() {
        return isPickedUp;
    }

    @Override
    public boolean canBePickedUp() {
        return true;
    }

    @Override
    public void hit(float amount) {
        setPickedUp(false);
        if (equals(GravityGun.heldEntity)) {
            GravityGun.heldEntity = null;
        }

        body.activate(true);
        PerspectiveCamera cam = ((InGameAccess) GameState.IN_GAME).getRawWorldCamera();
        body.setLinearVelocity(new Vector3f(cam.direction.cpy().scl(12).x, cam.direction.cpy().scl(12).y, cam.direction.cpy().scl(12).z));
    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        ((ExtendedBoundingBox) boundingBox).setInnerBounds(oBoundingBox);
        boundingBox.update();
    }

    boolean initialized = false;

    @Override
    public void update(Zone zone, double deltaTime) {
        if (!PhysicsThread.INSTANCE.shouldRun) return;
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
        MatrixUtil.rotateAroundOrigin3(oBoundingBox, transform, position, rotation);

        oBoundingBox.setBounds(rBoundingBox);
        oBoundingBox.setTransform(transform);

        if (!initialized) {
            PhysicsThread.alertChunk(zone.getChunkAtPosition(position));
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setPhysicsRotation(rotation);
            body.setMass(mass);
            initialized = true;

            PhysicsThread.addEntity(this);
            body.activate(true);
        } else {
            Vector3f vector3f = body.getPhysicsLocation(null);
            position = ConversionUtil.fromJME(vector3f);
            rotation = body.getPhysicsRotation(null);
//            body.setPhysicsRotation(rotation = new Quaternion());
        }

        if (canBePickedUp() && isPickedUp()) {
            Player player = InGame.getLocalPlayer();
            Vector3 playerPos = player.getPosition().cpy().add(0, 2, 0);
            PerspectiveCamera cam = InGameAccess.getAccess().getRawWorldCamera();
            playerPos.add(cam.direction.cpy().scl(2f));
            Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);

            Vector3f myPos = new Vector3f(position.x, position.y, position.z);
            Vector3f dir = new Vector3f(playerPosF);
            dir = dir.subtract(myPos).mult(3);

            body.setLinearVelocity(dir);
            body.activate(true);
        }

        if (!((ExtendedBoundingBox)localBoundingBox).hasInnerBounds()) {
            ((ExtendedBoundingBox)localBoundingBox).setInnerBounds(oBoundingBox);
        }

        getBoundingBox(globalBoundingBox);
        super.updateEntityChunk(zone);
    }

    @Override
    public void render(Camera worldCamera) {
        ChunkMeta meta = PhysicsThread.chunkMap.get(InGame.getLocalPlayer().getZone().getChunkAtPosition(position));
        if (meta != null)
            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), meta.getBody());

        DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), body);
        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            MatrixUtil.rotateAroundOrigin4(.5f, tmpModelMatrix, tmpRenderPos, rotation);
            if (modelInstance != null) {
                modelInstance.render(this, worldCamera, tmpModelMatrix);
            }
        }
    }

    @Override
    public void read(CRBinDeserializer deserial) {
        super.read(deserial);

        IPhysicEntity.read(this, deserial);
        ISingleEntityBlock.read(this, deserial);
        try {
            ((SingleBlockMesh) modelInstance).needsRemeshing = true;
        } catch (Exception ignore) {}

        body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
        body.setPhysicsRotation(rotation);
    }

    @Override
    public void write(CRBinSerializer serial) {
        super.write(serial);

        IPhysicEntity.write(this, serial);
        ISingleEntityBlock.write(this, serial);
    }

    @Override
    public @NonNull PhysicsBody getBody() {
        return body;
    }

    @Override
    public Quaternion getEularRotation() {
        return rotation;
    }

    @Override
    public @NonNull UUID getUUID() {
        return uuid;
    }

    @Override
    public float getMass() {
        return mass;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return shape;
    }

    @Override
    public void setEularRotation(Quaternion rot) {
        rotation.set(rot);
    }

    @Override
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void setMass(float mass) {
        this.mass = mass;
    }

    @Override
    public void setCollisionShape(CollisionShape shape) {
        this.shape = shape;
    }

    @Override
    public BlockState getState() {
        return state.get();
    }

    @Override
    public void setState(BlockState state) {
        this.state.set(state);
    }
}
