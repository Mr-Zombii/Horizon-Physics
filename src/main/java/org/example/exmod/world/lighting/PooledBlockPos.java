package org.example.exmod.world.lighting;

import com.badlogic.gdx.utils.Pool;
import org.example.exmod.world.BlockPos;
import org.example.exmod.world.Structure;

public class PooledBlockPos<TSelf extends BlockPos> extends BlockPos {
    Pool<TSelf> positionPool;

    public PooledBlockPos(Pool<TSelf> positionPool, Structure chunk, int localX, int localY, int localZ) {
        super(chunk, localX, localY, localZ);
        this.positionPool = positionPool;
    }

    public void free() {
        if (this.positionPool != null) {
            this.positionPool.free((TSelf) this);
        }

    }
}
