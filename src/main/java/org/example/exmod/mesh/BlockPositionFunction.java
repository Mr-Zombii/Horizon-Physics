package org.example.exmod.mesh;

import org.example.exmod.world.Structure;

public interface BlockPositionFunction<T> {
    T apply(Structure c, int x, int y, int z);
}
