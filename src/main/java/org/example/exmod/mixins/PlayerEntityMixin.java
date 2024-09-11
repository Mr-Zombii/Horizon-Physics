package org.example.exmod.mixins;

import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.PlayerEntity;
import finalforeach.cosmicreach.world.Zone;
import org.example.exmod.world.physics.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends Entity {

    @Override
    public void update(Zone zone, double delta) {
        PhysicsWorld.tick(delta);
//        PhysicsWorld.alertChunk(zone, currentChunk);
        super.update(zone, delta);
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
