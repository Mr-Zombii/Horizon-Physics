package me.zombii.horizon.worldgen;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blockdata.LayeredBlockData;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;
import finalforeach.cosmicreach.worldgen.ZoneGenerator;
import me.zombii.horizon.Constants;

public class NullGenerator extends ZoneGenerator {
    @Override
    public String getSaveKey() {
        return Constants.MOD_ID + ":null";
    }

    @Override
    protected String getName() {
        // Not fetched from the lang file
        return "null";
    }

    // Called on world load/create, after this.seed is set
    @Override
    public void create() {
        // Create noise generators
    }

    // Generate a chunk-column of the world at once (easier for the lighting engine this way)
    @Override
    public void generateForChunkColumn(Zone zone, ChunkColumn col) {
    }

    @Override
    public int getDefaultRespawnYLevel() {
        return -16;
    }
}
