package me.zombii.horizon.entity.player;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import me.zombii.horizon.threading.PhysicsThread;

import java.util.concurrent.atomic.AtomicReference;

public class PlayerInfo {

    public static AtomicReference<PhysicsRigidBody> bodyRef = new AtomicReference<>();
    public static AtomicReference<CollisionShape> shapeRef = new AtomicReference<>();

    public static PhysicsRigidBody body;
    public static CollisionShape shape;

    public static void init() {
        PhysicsThread.post(() -> {
            shapeRef.set(new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f)));
            shape = shapeRef.get();
            bodyRef.set(new PhysicsRigidBody(shape));
            body = bodyRef.get();
        });
    }

}
