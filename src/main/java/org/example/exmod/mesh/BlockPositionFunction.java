package org.example.exmod.mesh;

import com.github.puzzle.game.worldgen.structures.Structure;

public interface BlockPositionFunction<T> {
    T apply(Structure c, int x, int y, int z);
}
