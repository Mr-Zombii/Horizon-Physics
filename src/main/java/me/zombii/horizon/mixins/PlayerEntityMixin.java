package me.zombii.horizon.mixins;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.game.util.Reflection;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.entity.IVirtualWorldEntity;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.util.ConversionUtil;
import me.zombii.horizon.util.InGameAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends Entity {

    @Override
    public void update(Zone zone, double delta) {
        InGameAccess access = ((InGameAccess)InGame.IN_GAME);
        float maxRaycastDist = ((float)Reflection.getFieldContents(access.getBlockSelection(), "maximumRaycastDist")) + 3;

        Vector3f playerHead = ConversionUtil.toJME(position.cpy().add(0, 1.5f, 0));
        Vector3f raycastEnd = ConversionUtil.toJME(position.cpy().add(0, 1.5f, 0).add(access.getPlayerFacing().nor().scl(maxRaycastDist)));
        List<PhysicsRayTestResult> results = PhysicsThread.INSTANCE.space.rayTest(playerHead, raycastEnd);
        if (!results.isEmpty()) {
            float lowest = results.get(0).getHitFraction();
            PhysicsRayTestResult closest = results.get(0);
            for (PhysicsRayTestResult result : results) {
                if (result.getHitFraction() < lowest) {
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
//        PhysicsWorld.alertChunk(zone, currentChunk);
        super.update(zone, delta);
    }

    @Unique
    private float getLength(Vector3f a, Vector3f b) {
        return (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2) + Math.pow(b.z - a.z, 2));
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
