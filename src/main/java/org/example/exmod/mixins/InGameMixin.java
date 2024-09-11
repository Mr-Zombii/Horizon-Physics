package org.example.exmod.mixins;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import org.example.exmod.boundingBox.ExtendedBoundingBox;
import org.example.exmod.util.DebugRenderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(InGame.class)
public class InGameMixin {

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
            e.getBoundingBox(bb);

            if (((ExtendedBoundingBox) bb).hasInnerBounds()) {
                DebugRenderUtil.renderBoundingBox(sr, ((ExtendedBoundingBox) bb).getInnerBounds());
            } else {
                DebugRenderUtil.renderBoundingBox(sr, bb);
            }
        }

        sr.end();
    }

}
