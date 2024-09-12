package org.example.exmod.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.infos.ChildCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.gamestates.InGame;
import org.spongepowered.asm.mixin.Unique;

public class DebugRenderUtil {

    @Unique
    public static void renderBoundingBox(ShapeRenderer sh, Color color, OrientedBoundingBox bb){
        sh.setColor(color);
        renderBoxFromCorners(
                sh,
                bb.getCorner000(new Vector3()), bb.getCorner001(new Vector3()), bb.getCorner010(new Vector3()),
                bb.getCorner011(new Vector3()), bb.getCorner100(new Vector3()), bb.getCorner101(new Vector3()),
                bb.getCorner110(new Vector3()), bb.getCorner111(new Vector3())
        );
    }

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
            ShapeRenderer sr,
            Vector3 c000, Vector3 c001, Vector3 c010,
            Vector3 c011, Vector3 c100, Vector3 c101,
            Vector3 c110, Vector3 c111
    ) {
        try {
            sr.line(c000,c001);
            sr.line(c000,c100);
            sr.line(c000,c010);
            sr.line(c101,c001);
            sr.line(c101,c100);
            sr.line(c101,c111);
            sr.line(c011,c010);
            sr.line(c011,c111);
            sr.line(c011,c001);
            sr.line(c110,c111);
            sr.line(c110,c010);
            sr.line(c110,c100);
        } catch (Exception ignore) {
            sr.setProjectionMatrix(((InGameAccess)InGame.IN_GAME).getRawWorldCamera().combined);
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.line(c000,c001);
            sr.line(c000,c100);
            sr.line(c000,c010);
            sr.line(c101,c001);
            sr.line(c101,c100);
            sr.line(c101,c111);
            sr.line(c011,c010);
            sr.line(c011,c111);
            sr.line(c011,c001);
            sr.line(c110,c111);
            sr.line(c110,c010);
            sr.line(c110,c100);
            sr.end();
        }
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

    public static void renderRigidBody(ShapeRenderer sr, Vector3 vector3, PhysicsRigidBody rigidBody) {
        Vector3f pos = new Vector3f(vector3.x, vector3.y, vector3.z);
        Quaternion rot = rigidBody.getPhysicsRotation(new Quaternion());

//        pos.x += 0.5f;
//        pos.y += 0.5f;
//        pos.z += 0.5f;

        CompoundCollisionShape collisionShape = (CompoundCollisionShape) rigidBody.getCollisionShape();
        renderPhysicsShape(sr, pos, rot, collisionShape);
    }

    public static void renderRigidBody(ShapeRenderer sr, PhysicsRigidBody rigidBody) {
        Vector3f pos = rigidBody.getPhysicsLocation(new Vector3f());
        Quaternion rot = rigidBody.getPhysicsRotation(new Quaternion());

        pos.x += 0.5f;
        pos.y += 0.5f;
        pos.z += 0.5f;

        renderPhysicsShape(sr, pos, (CompoundCollisionShape) rigidBody.getCollisionShape());
    }

    public static void renderPhysicsShape(ShapeRenderer sr, Vector3f off, Quaternion rot, CompoundCollisionShape parent) {
        for (ChildCollisionShape child : parent.listChildren()) {
            Vector3f offs = off.add(child.copyOffset(new Vector3f()));

            if (child.getShape() instanceof CompoundCollisionShape) {
                renderPhysicsShape(sr, offs, rot, parent);
            } else {
                CollisionShape shape = child.getShape();
                com.jme3.bounding.BoundingBox box = shape.boundingBox(new Vector3f(), new Quaternion(), new com.jme3.bounding.BoundingBox());
                renderBoundingBox(sr, offs, rot, box);
            }
        }
    }

    public static void renderPhysicsShape(ShapeRenderer sr, Vector3f off, CompoundCollisionShape parent) {
        for (ChildCollisionShape child : parent.listChildren()) {
            Vector3f offs = off.add(child.copyOffset(new Vector3f()));

            if (child.getShape() instanceof CompoundCollisionShape) {
                renderPhysicsShape(sr, offs, parent);
            } else {
                CollisionShape shape = child.getShape();
                com.jme3.bounding.BoundingBox box = shape.boundingBox(offs, new Quaternion(), new com.jme3.bounding.BoundingBox());
                renderBoundingBox(sr, box);
            }
        }
    }

    public static void renderPhysicsShape(ShapeRenderer sr, CompoundCollisionShape parent) {
        for (ChildCollisionShape child : parent.listChildren()) {
            Vector3f offs = child.copyOffset(new Vector3f());

            if (child.getShape() instanceof CompoundCollisionShape) {
                renderPhysicsShape(sr, offs, parent);
            } else {
                CollisionShape shape = child.getShape();
                com.jme3.bounding.BoundingBox box = shape.boundingBox(offs, new Quaternion(), new com.jme3.bounding.BoundingBox());
                renderBoundingBox(sr, box);
            }
        }
    }

    private static void renderBoundingBox(ShapeRenderer sr, Vector3f offs, Quaternion quaternion, com.jme3.bounding.BoundingBox box) {
        Vector3 min = new Vector3();
        Vector3 max = new Vector3();

        Vector3f center = box.getCenter(new Vector3f());
        Vector3f extents = box.getExtent(new Vector3f());

        min.x = (center.x - extents.x);
        min.y = (center.y - extents.y);
        min.z = (center.z - extents.z);

        max.x = (center.x + extents.x);
        max.y = (center.y + extents.y);
        max.z = (center.z + extents.z);

        Matrix4 rotmat = new Matrix4();
        rotmat.idt();
        rotmat.set(new com.badlogic.gdx.math.Quaternion(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW()));

        Matrix4 transform = new Matrix4();
        transform.idt();

        OrientedBoundingBox boundingBox = new OrientedBoundingBox(new BoundingBox(min, max));
//        MatrixUtil.rotateAroundOrigin3(boundingBox, transform, new Vector3(offs.x, offs.y, offs.z).mul(rotmat), quaternion);
        boundingBox.setTransform(transform);

        renderBoundingBox(sr, Color.GREEN, boundingBox);
    }

    private static void renderBoundingBox(ShapeRenderer sr, com.jme3.bounding.BoundingBox box) {
        Vector3 min = new Vector3();
        Vector3 max = new Vector3();

        Vector3f center = box.getCenter(new Vector3f());
        Vector3f extents = box.getExtent(new Vector3f());

        min.x = (center.x - extents.x);
        min.y = (center.y - extents.y);
        min.z = (center.z - extents.z);

        max.x = (center.x + extents.x);
        max.y = (center.y + extents.y);
        max.z = (center.z + extents.z);

        OrientedBoundingBox boundingBox = new OrientedBoundingBox(new BoundingBox(min, max));

        renderBoundingBox(sr, Color.GREEN, boundingBox);
    }

}
