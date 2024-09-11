package org.example.exmod.world.lighting;

import com.badlogic.gdx.utils.Pool;
import finalforeach.cosmicreach.world.Chunk;
import org.example.exmod.world.BlockPos;
import org.example.exmod.world.Structure;

public class PooledBlockPosition<TSelf extends BlockPos> extends BlockPos {
    Pool<TSelf> positionPool;

    public PooledBlockPosition(Pool<TSelf> positionPool, Structure chunk, int localX, int localY, int localZ) {
        super(chunk, localX, localY, localZ);
        this.positionPool = positionPool;
    }

    public void free() {
        if (this.positionPool != null) {
            this.positionPool.free((TSelf) this);
        }

    }
}
