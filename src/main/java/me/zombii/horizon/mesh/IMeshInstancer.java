package me.zombii.horizon.mesh;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import me.zombii.horizon.Constants;
import me.zombii.horizon.world.PhysicsZone;
import me.zombii.horizon.world.VirtualWorld;

import java.util.concurrent.atomic.AtomicReference;

public interface IMeshInstancer {

    static IEntityModelInstance createSingleBlockMesh(AtomicReference<BlockState> state) {
        return Constants.MESHER_INSTANCE == null ? null : Constants.MESHER_INSTANCE.singleBlockMesh(state);
    }

    static IEntityModelInstance createMultiBlockMesh(VirtualWorld world) {
        return Constants.MESHER_INSTANCE == null ? null : Constants.MESHER_INSTANCE.multiBlockMesh(world);
    }

    static void genMesh(PhysicsZone physicsZone) {
        if (Constants.MESHER_INSTANCE == null) return;
        Constants.MESHER_INSTANCE.genMeshINST(physicsZone);
    }

    static IEntityModelInstance createZoneMesh(PhysicsZone zone) {
        return Constants.MESHER_INSTANCE == null ? null : Constants.MESHER_INSTANCE.zoneMesh(zone);
    }

    IEntityModelInstance multiBlockMesh(VirtualWorld world);
    IEntityModelInstance singleBlockMesh(AtomicReference<BlockState> state);
    IEntityModelInstance zoneMesh(PhysicsZone state);
    void genMeshINST(PhysicsZone zone);

}
