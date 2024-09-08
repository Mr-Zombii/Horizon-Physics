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
import org.example.exmod.boundingBox.OrientedBoundingBoxGetter;
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

            if (((OrientedBoundingBoxGetter) bb).hasInnerBounds()) {
                sr.setColor(Color.RED);
                exampleMod$boundingBoxToShapeRenderer(sr, ((OrientedBoundingBoxGetter) bb).getInnerBounds());
            } else {
                sr.setColor(Color.WHITE);
                exampleMod$boundingBoxToShapeRenderer(sr, bb);
//                sr.box(bb.min.x, bb.min.y, bb.min.z + bb.getDepth(), bb.getWidth(), bb.getHeight(), bb.getDepth());
            }
        }

        sr.end();
    }

    @Unique
    private static void exampleMod$boundingBoxToShapeRenderer(ShapeRenderer shapeRenderer, BoundingBox bb){
        Vector3 c000 = bb.getCorner000(new Vector3());
        Vector3 c001 = bb.getCorner001(new Vector3());
        Vector3 c010 = bb.getCorner010(new Vector3());
        Vector3 c011 = bb.getCorner011(new Vector3());
        Vector3 c100 = bb.getCorner100(new Vector3());
        Vector3 c101 = bb.getCorner101(new Vector3());
        Vector3 c110 = bb.getCorner110(new Vector3());
        Vector3 c111 = bb.getCorner111(new Vector3());
        shapeRenderer.line(c000,c001);
        shapeRenderer.line(c000,c100);
        shapeRenderer.line(c000,c010);
        shapeRenderer.line(c101,c001);
        shapeRenderer.line(c101,c100);
        shapeRenderer.line(c101,c111);
        shapeRenderer.line(c011,c010);
        shapeRenderer.line(c011,c111);
        shapeRenderer.line(c011,c001);
        shapeRenderer.line(c110,c111);
        shapeRenderer.line(c110,c010);
        shapeRenderer.line(c110,c100);
    }

    @Unique
    private static void exampleMod$boundingBoxToShapeRenderer(ShapeRenderer shapeRenderer, OrientedBoundingBox bb){
        Vector3 c000 = bb.getCorner000(new Vector3());
        Vector3 c001 = bb.getCorner001(new Vector3());
        Vector3 c010 = bb.getCorner010(new Vector3());
        Vector3 c011 = bb.getCorner011(new Vector3());
        Vector3 c100 = bb.getCorner100(new Vector3());
        Vector3 c101 = bb.getCorner101(new Vector3());
        Vector3 c110 = bb.getCorner110(new Vector3());
        Vector3 c111 = bb.getCorner111(new Vector3());
        shapeRenderer.line(c000,c001);
        shapeRenderer.line(c000,c100);
        shapeRenderer.line(c000,c010);
        shapeRenderer.line(c101,c001);
        shapeRenderer.line(c101,c100);
        shapeRenderer.line(c101,c111);
        shapeRenderer.line(c011,c010);
        shapeRenderer.line(c011,c111);
        shapeRenderer.line(c011,c001);
        shapeRenderer.line(c110,c111);
        shapeRenderer.line(c110,c010);
        shapeRenderer.line(c110,c100);
    }

}
