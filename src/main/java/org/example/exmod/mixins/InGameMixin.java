package org.example.exmod.mixins;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.example.exmod.bounds.ExtendedBoundingBox;
import org.example.exmod.util.DebugRenderUtil;
import org.example.exmod.util.InGameAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InGame.class)
public class InGameMixin implements InGameAccess {

    @Shadow private static ShapeRenderer sr;

    @Shadow private static BoundingBox bb;

    @Shadow private static PerspectiveCamera rawWorldCamera;

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
}
