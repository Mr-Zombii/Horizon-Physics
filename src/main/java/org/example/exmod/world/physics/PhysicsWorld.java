package org.example.exmod.world.physics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import org.example.exmod.entity.IPhysicEntity;
import org.example.exmod.util.CollisionMeshUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhysicsWorld {

    public static PhysicsSpace space;
    public static Queue<PhysicsBody> queuedObjects = new Queue<>();
    public static Map<UUID, IPhysicEntity> allEntities = new HashMap<>();
    public static Map<Chunk, ChunkMeta> chunkMetaMap = new HashMap<>();

    public static Logger LOGGER = LoggerFactory.getLogger("PhysicsWorld");

    public static void init() {
        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, -9.81f, 0));

        while (!queuedObjects.isEmpty()) {
            PhysicsBody body = queuedObjects.removeFirst();
            space.addCollisionObject(body);
        }
    }

    public static void tick(double delta) {
        if (space == null) init();

        while (!queuedObjects.isEmpty()) {
            PhysicsBody body = queuedObjects.removeFirst();
            space.addCollisionObject(body);
        }

        for (PhysicsRigidBody object : space.getRigidBodyList()) {
            System.out.println(object.getPhysicsLocation(null));
        }

        space.update((float) delta);
    }

    public static void invalidateChunk(Chunk chunk) {
        if (chunk == null || !chunkMetaMap.containsKey(chunk)) return;

        chunkMetaMap.get(chunk).setValidity(ChunkMeta.ChunkValidity.NEEDS_PHYSICS_REMESHING);
        for (UUID uuid : allEntities.keySet()) {
            IPhysicEntity entity = allEntities.get(uuid);
            try {
                entity.getBody().activate(true);
            } catch (Exception e) {
                LOGGER.warn("Null body found for entity \"{}\" with uuid \"{}\"", ((Entity) entity).entityTypeId, uuid);
            }
        }
    }

    public static void alertChunk(Zone zone, Chunk chunk) {
        if (chunk == null) return;
        addChunk(zone, chunk);
        Array<Chunk> adjacentChunks = new Array<>();
        chunk.getAdjacentChunks(zone, adjacentChunks);
        while (!adjacentChunks.isEmpty()) {
            Chunk c = adjacentChunks.pop();
            addChunk(zone, c);
        }
    }

    public static void addChunk(Zone zone, Chunk chunk) {
        if (chunk == null) return;

//        for (IPhysicEntity entity : allEntities.values()) {
//            Chunk currentChunk = zone.getChunkAtPosition(((Entity) entity).position);
//            if (chunk == currentChunk) entity.getBody().setMass(0);
//        }

        boolean chunkExists = chunkMetaMap.containsKey(chunk);
        ChunkMeta meta = chunkMetaMap.get(chunk);

        if (chunkExists && meta != null && meta.getValidity() == ChunkMeta.ChunkValidity.HAS_RIGID_BODY && !meta.hasNullBody()) return;
        else if (meta == null) meta = new ChunkMeta();

        IBlockData<BlockState> blocks = chunk.blockData;
        if (blocks == null || blocks.isEntirely((b) -> !b.walkThrough)) return;

        CompoundCollisionShape shape = CollisionMeshUtil.createPhysicsMesh(chunk);
        meta.setValidity(ChunkMeta.ChunkValidity.HAS_RIGID_BODY);
        if (!chunkExists) {
            PhysicsRigidBody body = new PhysicsRigidBody(shape, 0);
            body.setPhysicsLocation(new Vector3f(chunk.blockX, chunk.blockY, chunk.blockZ));
            meta.setBody(addPhysicsBody(body));
            chunkMetaMap.put(chunk, meta);
        } else {
            meta.getBody().setCollisionShape(shape);
        }

//        for (IPhysicEntity entity : allEntities.values()) {
//            Chunk currentChunk = zone.getChunkAtPosition(((Entity) entity).position);
//            if (chunk == currentChunk) entity.getBody().setMass(entity.getMass());
//        }

    }

    public static <T extends Entity & IPhysicEntity> T addEntity(T entity) {
        allEntities.put(entity.getUUID(), entity);
        addPhysicsBody(entity.getBody());
        return entity;
    }

    public static <T extends Entity & IPhysicEntity> T removeEntity(T entity) {
        allEntities.remove(entity.getUUID());
        if (space != null) space.removeCollisionObject(entity.getBody());
        return entity;
    }

    private static <T extends PhysicsBody> T addPhysicsBody(T body) {
        if (space == null) {
            queuedObjects.addLast(body);
            return body;
        }
        space.addCollisionObject(body);
        return body;
    }

}
