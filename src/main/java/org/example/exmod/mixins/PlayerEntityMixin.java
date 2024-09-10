package org.example.exmod.mixins;

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
        PhysicsWorld.alertChunk(zone, zone.getChunkAtPosition(position));
        super.update(zone, delta);
    }

}
