package me.zombii.horizon.util;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.game.util.Reflection;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.ui.UI;
import me.zombii.horizon.entity.api.IVirtualWorldEntity;
import me.zombii.horizon.threading.PhysicsThread;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

public class PhysicsUtil {

    public static void raycast(Vector3 from, Vector3 to, TriConsumer<Float, Entity, PhysicsRayTestResult> consumer) {
        from = from.cpy();
        to = to.cpy();

        InGameAccess access = InGameAccess.getAccess();
        float maxRaycastDist = ((float) Reflection.getFieldContents(access.getBlockSelection(), "maximumRaycastDist")) + 3;

        Vector3f fromF = ConversionUtil.toJME(from);
        Vector3f toF = ConversionUtil.toJME(to);
        List<PhysicsRayTestResult> results = PhysicsThread.INSTANCE.space.rayTest(fromF, toF);
        if (!results.isEmpty()) {
            float lowest = results.get(0).getHitFraction();
            PhysicsRayTestResult closest = results.get(0);
            for (PhysicsRayTestResult result : results) {
                if (result.getHitFraction() < lowest) {
                    closest = result;
                    lowest = result.getHitFraction();
                }
            }
            Entity e = PhysicsThread.getByBody((PhysicsBody) closest.getCollisionObject());

            if (e != null) {
                Matrix4 rotMat = new Matrix4();
                Quaternion quaternion = closest.getCollisionObject().getPhysicsRotation(null);
                rotMat.idt();
                rotMat.set(ConversionUtil.fromJME(quaternion));

                float dist = (getLength(toF, fromF) * closest.getHitFraction());
                consumer.accept(dist, e, closest);
            }
        }
    }

    public static float getLength(Vector3f a, Vector3f b) {
        return (float) Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2) + Math.pow(b.z - a.z, 2));
    }

}
