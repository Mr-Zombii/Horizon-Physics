package me.zombii.horizon.util;

import me.zombii.horizon.world.VirtualChunk;

public interface BlockPositionFunction<T> {
    T apply(VirtualChunk c, int x, int y, int z);
}
