package org.example.exmod.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.math.Vector3f;
import org.spongepowered.asm.mixin.Unique;

public class DebugRenderUtil {

    @Unique
    public static void renderBoundingBox(ShapeRenderer sh, OrientedBoundingBox bb){
        sh.setColor(Color.RED);
        renderBoxFromCorners(
                sh,
                bb.getCorner000(new Vector3()), bb.getCorner001(new Vector3()), bb.getCorner010(new Vector3()),
                bb.getCorner011(new Vector3()), bb.getCorner100(new Vector3()), bb.getCorner101(new Vector3()),
                bb.getCorner110(new Vector3()), bb.getCorner111(new Vector3())
        );
    }

    @Unique
    public static void renderBoundingBox(ShapeRenderer sh, BoundingBox bb){
        sh.setColor(Color.WHITE);
        renderBoxFromCorners(
                sh,
                bb.getCorner000(new Vector3()), bb.getCorner001(new Vector3()), bb.getCorner010(new Vector3()),
                bb.getCorner011(new Vector3()), bb.getCorner100(new Vector3()), bb.getCorner101(new Vector3()),
                bb.getCorner110(new Vector3()), bb.getCorner111(new Vector3())
        );
    }

    public static void renderBoxFromCorners(
            ShapeRenderer sh,
            Vector3 c000, Vector3 c001, Vector3 c010,
            Vector3 c011, Vector3 c100, Vector3 c101,
            Vector3 c110, Vector3 c111
    ) {
        sh.line(c000,c001);
        sh.line(c000,c100);
        sh.line(c000,c010);
        sh.line(c101,c001);
        sh.line(c101,c100);
        sh.line(c101,c111);
        sh.line(c011,c010);
        sh.line(c011,c111);
        sh.line(c011,c001);
        sh.line(c110,c111);
        sh.line(c110,c010);
        sh.line(c110,c100);
    }

    public static void renderBoxFromCorners(
            ShapeRenderer sh,
            Vector3f c000, Vector3f c001, Vector3f c010,
            Vector3f c011, Vector3f c100, Vector3f c101,
            Vector3f c110, Vector3f c111
    ) {
        Vector3 b000 = new Vector3(c000.x, c000.y, c000.z);
        Vector3 b001 = new Vector3(c001.x, c001.y, c001.z);
        Vector3 b010 = new Vector3(c010.x, c010.y, c010.z);
        Vector3 b011 = new Vector3(c011.x, c011.y, c011.z);
        Vector3 b100 = new Vector3(c100.x, c100.y, c100.z);
        Vector3 b101 = new Vector3(c101.x, c101.y, c101.z);
        Vector3 b110 = new Vector3(c110.x, c110.y, c110.z);
        Vector3 b111 = new Vector3(c111.x, c111.y, c111.z);
        renderBoxFromCorners(sh, b000, b001, b010, b011, b100, b101, b110, b111);
    }

}
