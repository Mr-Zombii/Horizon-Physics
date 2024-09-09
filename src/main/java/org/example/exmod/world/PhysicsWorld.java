package org.example.exmod.world;

import com.badlogic.gdx.utils.Queue;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.entities.Entity;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.example.exmod.entity.IPhysicEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhysicsWorld {

    public static PhysicsSpace space;
    public static Queue<PhysicsBody> queuedObjects = new Queue<>();
    public static Map<UUID, Entity> AllEntities = new HashMap<>();

    public static void init() {
        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, 0, 0));

        while (!queuedObjects.isEmpty()) {
            PhysicsBody body = queuedObjects.removeFirst();
            space.addCollisionObject(body);
        }
    }

    public static void tick(double delta) {
        if (space == null) init();
        space.update((float) delta);
    }

    public static <T extends Entity & IPhysicEntity> void addEntity(T entity) {
        AllEntities.put(entity.getUUID(), entity);
        addPhysicsBody(entity.getBody());
    }

    public static <T extends Entity & IPhysicEntity> void removeEntity(T entity) {
        AllEntities.remove(entity.getUUID());
        if (space != null) space.removeCollisionObject(entity.getBody());
    }

    private static void addPhysicsBody(PhysicsBody body) {
        if (space == null) {
            queuedObjects.addLast(body);
            return;
        }
        space.addCollisionObject(body);
    }

}
