package me.zombii.horizon.mixins;

import finalforeach.cosmicreach.server.ServerLauncher;
import me.zombii.horizon.threading.PhysicsThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLauncher.class)
public class ServerLauncherPhysicsLoader {

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/networking/netty/NettyServer;run()V", shift = At.Shift.BEFORE))
    private static void init(String[] args, CallbackInfo ci) {
        PhysicsThread.start();
    }

}
