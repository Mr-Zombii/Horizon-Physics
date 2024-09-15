package me.zombii.horizon.mixins;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import me.zombii.horizon.bounds.ExtendedBoundingBox;
import me.zombii.horizon.threading.PhysicsThread;
import me.zombii.horizon.threading.ThreadHelper;
import me.zombii.horizon.util.DebugRenderUtil;
import me.zombii.horizon.util.InGameAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixin implements InGameAccess {

    @Shadow private static ShapeRenderer sr;

    @Shadow private static BoundingBox bb;

    @Shadow private static PerspectiveCamera rawWorldCamera;

    @Shadow private BlockSelection blockSelection;

    /**
     * @author Mr_Zombii
     * @reason Render Special Bounding Boxes
     */
    @Overwrite
    private static void drawEntityDebugBoundingBoxes(Zone playerZone) {
        if (sr == null) {
            sr = new ShapeRenderer();
            bb = new BoundingBox();
        }

        sr.setProjectionMatrix(rawWorldCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);

        for(Entity e : playerZone.allEntities) {
            try {
                e.getBoundingBox(bb);

                if (((ExtendedBoundingBox) bb).hasInnerBounds()) {
                    DebugRenderUtil.renderBoundingBox(sr, ((ExtendedBoundingBox) bb).getInnerBounds());
                } else {
                    DebugRenderUtil.renderBoundingBox(sr, bb);
                }
            } catch (Exception ignore) {}
        }

        sr.end();
    }

    @Override
    public ShapeRenderer getShapeRenderer() {
        if (sr != null) return sr;
        sr = new ShapeRenderer();
        return sr;
    }

    @Override
    public PerspectiveCamera getRawWorldCamera() {
        return rawWorldCamera;
    }

    @Override
    public Vector3 getPlayerFacing() {
        return rawWorldCamera.direction.cpy();
    }

    @Override
    public BlockSelection getBlockSelection() {
        return blockSelection;
    }

    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void exitWorld(CallbackInfo ci) {
        PhysicsThread.clear();
    }

    @Inject(method = "loadWorld(Lfinalforeach/cosmicreach/world/World;)V", at = @At("HEAD"))
    private void joinWorld(World world, CallbackInfo ci) {
        PhysicsThread.start();
    }

    @Inject(method = "dispose", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/InGame;unloadWorld()V", shift = At.Shift.AFTER))
    private void dispose(CallbackInfo ci) {
        ThreadHelper.killAll();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/InGame;switchToGameState(Lfinalforeach/cosmicreach/gamestates/GameState;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void pause(CallbackInfo ci) {
        PhysicsThread.pause();
    }

}
