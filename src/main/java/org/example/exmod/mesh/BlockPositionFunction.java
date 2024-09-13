package org.example.exmod.mesh;

import org.example.exmod.world.VirtualChunk;

public interface BlockPositionFunction<T> {
    T apply(VirtualChunk c, int x, int y, int z);
}
