package me.zombii.horizon.mixins;

import me.zombii.horizon.threading.PhysicsThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "finalforeach/cosmicreach/gamestates/PauseMenu$2")
public class ResumeToGameMixin {

    @Inject(method = "onClick", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/GameState;switchToGameState(Lfinalforeach/cosmicreach/gamestates/GameState;)V"))
    private void backToGame(CallbackInfo ci) {
        PhysicsThread.resume();
    }

}
