package me.zombii.horizon.mesh;

import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.rendering.ChunkMeshGroup;
import finalforeach.cosmicreach.rendering.IChunkMeshGroup;
import finalforeach.cosmicreach.rendering.IMeshData;
import finalforeach.cosmicreach.rendering.MeshData;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import me.zombii.horizon.world.PhysicsZone;
import me.zombii.horizon.world.VirtualWorld;

import java.util.concurrent.atomic.AtomicReference;

public class MeshInstancer implements IMeshInstancer {

    @Override
    public IEntityModelInstance multiBlockMesh(VirtualWorld world) {
        return new MutliBlockMesh(world);
    }

    @Override
    public IEntityModelInstance singleBlockMesh(AtomicReference<BlockState> state) {
        return new SingleBlockMesh(state);
    }

    @Override
    public IEntityModelInstance zoneMesh(PhysicsZone zone) {
        return new ZoneMesh(zone);
    }

    @Override
    public void genMeshINST(PhysicsZone zone) {
        zone.chunks.forEach(c -> {
            Array<MeshData> mesh = ((ChunkMeshGroup)c.getMeshGroup()).buildMeshVertices(c);
            ((ChunkMeshGroup) c.getMeshGroup()).setMeshVertices(c, mesh);
        });
    }
}
