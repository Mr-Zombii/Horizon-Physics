package org.example.exmod.threading;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PauseableThread;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.rendering.MeshData;
import org.example.exmod.util.CosmicMeshingUtil;
import org.example.exmod.world.VirtualChunk;
import org.example.exmod.world.VirtualWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class MeshingThread implements Runnable {

    public static Logger LOGGER = LoggerFactory.getLogger("Horizon Meshing Thread");
    public static MeshingThread INSTANCE;
    public static PauseableThread parent;

    static final Queue<Runnable> queuedRunnables = new Queue<>();

    public MeshingThread() {
        INSTANCE = this;
        parent = (PauseableThread) ThreadHelper.getThread("meshing");
    }

    @Override
    public void run() {
        synchronized (queuedRunnables) {
            while (!queuedRunnables.isEmpty()) {
                Runnable runnable = queuedRunnables.removeFirst();
                if (runnable != null)
                    runnable.run();
                else
                    LOGGER.warn("Uh oh, A null runnable was found on the `MeshingThread`");
            }
        }
        parent.onPause();
    }

    public static boolean started = false;

    public static AtomicReference<Array<MeshData>> post(AtomicReference<Array<MeshData>> ref, VirtualChunk chunk) {
        if (chunk.parent == null)
            throw new RuntimeException(VirtualChunk.class.getSimpleName() + " must have a parent " + VirtualWorld.class.getSimpleName());
        if (chunk.isEntirely(Block.AIR.getDefaultBlockState()))
            return ref;

        queuedRunnables.addLast(() -> {
            synchronized (ref) {
                chunk.needsRemeshing = false;
                Array<MeshData> array = CosmicMeshingUtil.getMeshData(chunk.parent, chunk.chunkPos, chunk);
                ref.set(array);
                chunk.needsToRebuildMesh = true;
            }
        });

        if (!started) start();
        else parent.onResume();

        return ref;
    }

    public static AtomicReference<Array<MeshData>> post(VirtualChunk chunk) {
        return post(new AtomicReference<>(), chunk);
    }

    public static void clear() {
        synchronized (queuedRunnables) {
            queuedRunnables.clear();
        }

        parent.onPause();
    }

    public static void pause() {
        parent.onPause();
    }

    public static void resume() {
        parent.onResume();
    }

    public static void init() {
        parent = ThreadHelper.createPauseable("meshing", new MeshingThread());
    }

    public static PauseableThread start() {
        started = true;
        if (parent == null) {
            throw new RuntimeException("Call `init()` on the `MeshingThread` first.");
        }
//        try {
        parent.start();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return parent;
    }
}
