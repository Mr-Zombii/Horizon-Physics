package me.zombii.horizon.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import finalforeach.cosmicreach.rendering.IZoneRenderer;
import finalforeach.cosmicreach.rendering.WorldRenderingMeshGenThread;
import finalforeach.cosmicreach.world.Chunk;
import me.zombii.horizon.threading.PhysicsThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderingMeshGenThread.class)
public class CollisionMeshInvalidator {

    @Inject(method = "meshChunks", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/rendering/IZoneRenderer;onChunkMeshed(Lfinalforeach/cosmicreach/world/Chunk;)V", shift = At.Shift.BEFORE))
    private void onChunkMeshed(IZoneRenderer zoneRenderer, CallbackInfo ci, @Local Chunk chunk) {
        PhysicsThread.invalidateChunk(chunk);
    }

}
