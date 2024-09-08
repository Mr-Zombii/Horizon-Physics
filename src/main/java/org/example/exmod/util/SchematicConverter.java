package org.example.exmod.util;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.game.worldgen.schematics.Schematic;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.blocks.BlockState;
import org.example.exmod.Constants;
import org.example.exmod.structures.Structure;

import java.util.HashMap;
import java.util.Map;

public class SchematicConverter {

    public static Map<Vec3i, Structure> structureMapFromSchematic(Schematic schematic) {
        Map<Vec3i, Structure> structureMap = new HashMap<>();

        Structure structure = new Structure((short) 0, new Identifier(Constants.MOD_ID, "0"));

        Vec3i lastChunkCoord = new Vec3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vec3i currentChunkCoord;
        for (int x = 0; x < schematic.length; x++) {
            for (int y = 0; y < schematic.height; y++) {
                for (int z = 0; z < schematic.width; z++) {
                    BlockState state = schematic.getBlockState(x, y, z);

                    int cx = Math.floorDiv(x, 16);
                    int cy = Math.floorDiv(y, 16);
                    int cz = Math.floorDiv(z, 16);
                    currentChunkCoord = new Vec3i(cx, cy, cz);

                    int lx = x - (cx * 16);
                    int ly = y - (cy * 16);
                    int lz = z - (cz * 16);

//                    if (lx == 15 && ly == 15 && lz == 15) {
                    if (lastChunkCoord != currentChunkCoord) {
                        structureMap.put(new Vec3i(cx, cy ,cz), structure);
                        structure = new Structure((short) 0, new Identifier(Constants.MOD_ID, "1"));
                        lastChunkCoord = currentChunkCoord;
                    } else {
                        structure.setBlockState(state, lx, ly, lz);
                    }
                }
            }
        }

        return structureMap;
    }

}
