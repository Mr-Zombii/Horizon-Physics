package org.example.exmod.world.lighting;

import com.badlogic.gdx.utils.Pool;
import org.example.exmod.world.BlockPos;
import org.example.exmod.world.VirtualChunk;

class PooledLightLimitPos extends PooledBlockPos<PooledLightLimitPos> {
    int lightLimit = 0;

    public PooledLightLimitPos(Pool<PooledLightLimitPos> positionPool, VirtualChunk chunk, int localX, int localY, int localZ) {
        super(positionPool, chunk, localX, localY, localZ);
    }

    public void free() {
        super.free();
        this.lightLimit = 0;
    }

    public BlockPos set(BlockPos pos) {
        if (pos instanceof PooledLightLimitPos l) {
            this.lightLimit = l.lightLimit;
        } else {
            this.lightLimit = 0;
        }

        return super.set(pos);
    }

    public int getRedLimit() {
        return (this.lightLimit & 3840) >> 8;
    }

    public int getGreenLimit() {
        return (this.lightLimit & 240) >> 4;
    }

    public int getBlueLimit() {
        return this.lightLimit & 15;
    }

    public int getCombinedLimit(int inputLimit) {
        int r = (inputLimit & 3840) >> 8;
        int g = (inputLimit & 240) >> 4;
        int b = inputLimit & 15;
        int newR = Math.min(r, this.getRedLimit());
        int newG = Math.min(g, this.getGreenLimit());
        int newB = Math.min(b, this.getBlueLimit());
        return (newR << 8) + (newG << 4) + newB;
    }
}
