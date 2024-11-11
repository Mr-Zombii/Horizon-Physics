package me.zombii.horizon.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.core.loader.util.Reflection;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.entity.api.IPhysicEntity;
import me.zombii.horizon.entity.api.IVirtualWorldEntity;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import me.zombii.horizon.world.physics.ChunkMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

import static me.zombii.horizon.entity.player.PlayerInfo.body;
import static me.zombii.horizon.entity.player.PlayerInfo.shape;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends Entity implements IPhysicEntity {

    @Shadow public transient Player player;

    public PlayerEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Unique
    private void horizonPhysics$init() {
        hasGravity = false;
    }

    @Inject(method = "<init>(Lfinalforeach/cosmicreach/entities/player/Player;)V", at = @At("TAIL"))
    private void init0(Player player, CallbackInfo ci) {
        horizonPhysics$init();
    }

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void init0(CallbackInfo ci) {
        horizonPhysics$init();
    }

    boolean hasInit = false;

    @Override
    public void updateConstraints(Zone zone, Vector3 targetPosition) {

    }

    private transient UUID uuid = UUID.randomUUID();

    private Vector3 acceleration = Vector3.Zero.cpy();
    private Vector3 posDiff = Vector3.Zero.cpy();
    private Vector3 targetPosition = Vector3.Zero.cpy();

    @Override
    public void render(Camera worldCamera) {
        ChunkMeta meta = PhysicsThread.chunkMap.get(InGame.getLocalPlayer().getZone().getChunkAtPosition(position));
        if (meta != null)
            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), meta.getBody());
        if (body != null) {
            DebugRenderUtil.renderRigidBody(InGameAccess.getAccess().getShapeRenderer(), body);
        }

        if (!GameSingletons.isClient || GameSingletons.client().getLocalPlayer() != this.player) {
            super.render(worldCamera);
        }
    }

    @Override
    public void update(Zone zone, double delta) {
        PhysicsThread.alertChunk(zone.getChunkAtPosition(position));

        if (body != null) {
            if (this.currentChunk != null) {
                this.lastPosition.set(this.position);
                float ax = this.acceleration.x * (float) delta;
                float ay = this.acceleration.y * (float) delta;
                float az = this.acceleration.z * (float) delta;
                this.velocity.add(ax, ay, az);
                this.velocity.add(this.onceVelocity);
                float vx = this.velocity.x * (float) delta;
                float vy = this.velocity.y * (float) delta;
                float vz = this.velocity.z * (float) delta;
                this.posDiff.set(vx, vy, vz);
                this.targetPosition.set(this.position).add(this.posDiff);
            }

            if (!hasInit) {
                PhysicsThread.addEntity(this);
                body.setPhysicsLocation(ConversionUtil.toJME(position));
                //        body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
                body.setMass(5);

                body.activate(true);
                hasInit = true;
            } else {
                if (noClip) {
                    this.position.add(this.posDiff);

                    body.setPhysicsLocation(ConversionUtil.toJME(position));
                } else {
                    position = ConversionUtil.fromJME(body.getPhysicsLocation(null));
                    System.out.println(body.getPhysicsLocation(null));
                }
                body.setPhysicsRotation(new Quaternion());

                this.velocity.sub(this.onceVelocity);
                this.acceleration.setZero();
                this.onceVelocity.setZero();
            }

            if (!PhysicsThread.allEntities.containsValue(this)) {
                PhysicsThread.addEntity(this);
            }
        }

        updateEntityChunk(zone);

        InGameAccess access = InGameAccess.getAccess();
        float maxRaycastDist = ((float) Reflection.getFieldContents(access.getBlockSelection(), "maximumRaycastDist")) + 3;

        Vector3f playerHead = ConversionUtil.toJME(position.cpy().add(0, 1.5f, 0));
        Vector3f raycastEnd = ConversionUtil.toJME(position.cpy().add(0, 1.5f, 0).add(access.getPlayerFacing().nor().scl(maxRaycastDist)));
        List<PhysicsRayTestResult> results = PhysicsThread.INSTANCE.space.rayTest(playerHead, raycastEnd);
        if (!results.isEmpty()) {
            float lowest = results.get(0).getHitFraction();
            PhysicsRayTestResult closest = results.get(0);
            for (PhysicsRayTestResult result : results) {
                if (result.getHitFraction() < lowest && PhysicsThread.getByBody((PhysicsBody) closest.getCollisionObject()) instanceof IVirtualWorldEntity) {
                    closest = result;
                    lowest = result.getHitFraction();
                }
            }
            Entity e = PhysicsThread.getByBody((PhysicsBody) closest.getCollisionObject());

            BoundingBox bb = new BoundingBox();
            if (e instanceof IVirtualWorldEntity entity) {
                e.getBoundingBox(bb);

                Matrix4 rotMat = new Matrix4();
                Quaternion quaternion = closest.getCollisionObject().getPhysicsRotation(null);
                rotMat.idt();

                rotMat.set(ConversionUtil.fromJME(quaternion));

                float dist = (getLength(raycastEnd, playerHead) * closest.getHitFraction());
                Vector3 distVec = access.getPlayerFacing().nor().scl(dist);
                Vector3 hitPosition = position.cpy().add(0, 1.5f, 0).add(distVec);
                Vector3 innerEntityPosition = hitPosition.cpy().sub(e.position.cpy()).unrotate(rotMat);

                BlockState state = entity.getWorld().getBlockstateAt(innerEntityPosition);
                if (ControlSettings.keyUsePlace.isPressed())
                    if (UI.hotbar.getSelectedItemStack() != null && UI.hotbar.getSelectedItemStack().getItem() instanceof ItemBlock block) {
                        entity.getWorld().setBlockState(innerEntityPosition, block.getBlockState());
                    }
                if (ControlSettings.keyAttackBreak.isPressed())
                    entity.getWorld().setBlockState(innerEntityPosition, BlockState.getInstance("base:air[default]"));

                System.out.println(state + " " + hitPosition + " " + innerEntityPosition + " " + dist + " " + (1 - closest.getHitFraction()));
            }
        }

    }

    @Unique
    private float getLength(Vector3f a, Vector3f b) {
        return (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2) + Math.pow(b.z - a.z, 2));
    }

    @Override
    public @NonNull PhysicsBody getBody() {
        return body;
    }

    @Override
    public Quaternion getEularRotation() {
        return new Quaternion();
    }

    @Override
    public @NonNull UUID getUUID() {
        return uuid;
    }

    @Override
    public float getMass() {
        return 10;
    }

    @Override
    public CollisionShape getCollisionShape() {
        return shape;
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

//    @Override
//    public void updateConstraints(Zone zone, Vector3 targetPosition) {
//        this.tmpEntityBoundingBox.set(this.localBoundingBox);
//        this.tmpEntityBoundingBox.min.add(this.position);
//        this.tmpEntityBoundingBox.max.add(this.position);
//        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y;
//        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
//        this.tmpEntityBoundingBox.update();
//    }

}
