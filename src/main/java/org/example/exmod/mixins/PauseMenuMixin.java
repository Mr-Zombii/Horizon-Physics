package org.example.exmod.mixins;

import finalforeach.cosmicreach.gamestates.PauseMenu;
import org.example.exmod.threading.PhysicsThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseMenu.class)
public class PauseMenuMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/PauseMenu;switchToGameState(Lfinalforeach/cosmicreach/gamestates/GameState;)V"))
    private void backToGame(CallbackInfo ci) {
        PhysicsThread.resume();
    }

}
