package org.example.exmod.threading;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Queue;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.savelib.blockdata.IBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import org.example.exmod.entity.IPhysicEntity;
import org.example.exmod.util.CollisionMeshUtil;
import org.example.exmod.util.NativeLibraryLoader;
import org.example.exmod.world.physics.ChunkMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhysicsThread implements TickingRunnable {

    public static Logger LOGGER = LoggerFactory.getLogger("Horizon Physics Thread");
    public static PhysicsThread INSTANCE;
    public static final Queue<PhysicsBody> queuedObjects = new Queue<>();
    public static final Map<Chunk, ChunkMeta> chunkMap = new HashMap<>();
    public static final Map<UUID, IPhysicEntity> allEntities = new HashMap<>();
    public static final Queue<PhysicsBody> bodiesToRemove = new Queue<>();

    public PhysicsSpace space;
    public static PauseableThread parent;

    public PhysicsThread() {
        INSTANCE = this;
        parent = (PauseableThread) ThreadHelper.getThread("physics");
    }

    boolean isInitialized = false;
    public boolean shouldRun = false;

    void physicsInit() {
        if (isInitialized) return;
        isInitialized = true;
        boolean success = NativeLibraryLoader.loadLibbulletjme("Release", "Sp");
        if (!success) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }

        space = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        space.setGravity(new Vector3f(0, -9.81f, 0));

        synchronized (queuedObjects) {
            while (!queuedObjects.isEmpty()) {
                space.addCollisionObject(queuedObjects.removeFirst());
            }
        }
    }

    public static void alertChunk(final Chunk chunk) {
        if (chunk == null) return;
        INSTANCE.meshChunk(chunk);
        Array<Chunk> adjacentChunks = new Array<>();
        chunk.getAdjacentChunks(chunk.region.zone, adjacentChunks);
        while (!adjacentChunks.isEmpty()) {
            Chunk c = adjacentChunks.pop();
            INSTANCE.meshChunk(c);
        }
    }

    void meshChunk(final Chunk chunk) {
        synchronized (chunk.region.zone) {
            ChunkMeta meta = chunkMap.get(chunk);
            boolean chunkExist = meta != null;

            if (chunkExist && meta.getValidity() == ChunkMeta.ChunkValidity.HAS_RIGID_BODY && !meta.hasNullBody()) return;
            if (!chunkExist) meta = new ChunkMeta();

            IBlockData<BlockState> blocks = chunk.blockData;
            if (blocks == null || blocks.isEntirely((b) -> !b.walkThrough)) return;

            CompoundCollisionShape shape = CollisionMeshUtil.createPhysicsMesh(chunk);
            meta.setValidity(ChunkMeta.ChunkValidity.HAS_RIGID_BODY);
            if (!chunkExist) {
                PhysicsRigidBody body = new PhysicsRigidBody(shape, 0);
                body.setPhysicsLocation(new Vector3f(chunk.blockX, chunk.blockY, chunk.blockZ));
                meta.setBody(body);
                addPhysicsBody(body);
                chunkMap.put(chunk, meta);
                return;
            }
            meta.getBody().setCollisionShape(shape);
        }
    }

    public static <T extends Entity & IPhysicEntity> T addEntity(T entity) {
        allEntities.put(entity.getUUID(), entity);
        addPhysicsBody(entity.getBody());
        return entity;
    }

    public static <T extends Entity & IPhysicEntity> T removeEntity(T entity) {
        allEntities.remove(entity.getUUID());
        bodiesToRemove.addLast(entity.getBody());
        return entity;
    }

    public static void invalidateChunk(Chunk chunk) {
        if (chunk == null || !chunkMap.containsKey(chunk)) return;

        chunkMap.get(chunk).setValidity(ChunkMeta.ChunkValidity.NEEDS_PHYSICS_REMESHING);
        for (UUID uuid : allEntities.keySet()) {
            IPhysicEntity entity = allEntities.get(uuid);
            try {
                entity.getBody().activate(true);
            } catch (Exception e) {
                LOGGER.warn("Null body found for entity \"{}\" with uuid \"{}\"", ((Entity) entity).entityTypeId, uuid);
            }
        }
    }

    private static <T extends PhysicsBody> T addPhysicsBody(T body) {
        queuedObjects.addLast(body);
        return body;
    }

    @Override
    public void run(float delta) {
        if (!isInitialized) physicsInit();
        if (shouldRun) {
            synchronized (bodiesToRemove) {
                while (!bodiesToRemove.isEmpty()) {
                    space.removeCollisionObject(bodiesToRemove.removeFirst());
                }
            }

            synchronized (queuedObjects) {
                while (!queuedObjects.isEmpty()) {
                    space.addCollisionObject(queuedObjects.removeFirst());
                }
            }

            space.update(delta);
        }
    }

    public static void clear() {
        synchronized (queuedObjects) {
            queuedObjects.clear();
            chunkMap.clear();
            allEntities.clear();
        }

        INSTANCE.shouldRun = false;
        parent.onPause();
    }

    public static void pause() {
        INSTANCE.shouldRun = false;
        parent.onPause();
    }

    public static boolean started = false;

    public static void resume() {
        INSTANCE.shouldRun = true;
        parent.onResume();
    }

    public static void init() {
        parent = ThreadHelper.createTicking("physics", new PhysicsThread());
    }

    public static PauseableThread start() {
        if (started) {
            INSTANCE.shouldRun = true;
            resume();
            return null;
        }
        if (parent == null) {
            throw new RuntimeException("Call `init()` on the `PhysicsThread` first.");
        }

        parent.start();
        INSTANCE.shouldRun = true;

        started = true;
        return parent;
    }

}
