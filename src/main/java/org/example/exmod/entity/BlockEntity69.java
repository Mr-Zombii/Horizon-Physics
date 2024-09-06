package org.example.exmod.entity;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.game.worldgen.structures.Structure;
import com.github.puzzle.game.worldgen.structures.StructureFormat;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import org.example.exmod.mesh.BlockModelv2;

import java.util.HashMap;
import java.util.Map;

// /summon funni-blocks:entity

public class BlockEntity69 extends Entity {

    Map<Vec3i, Structure> chunks = new HashMap<>();

    public BlockEntity69() {

        Vec3i z = new Vec3i(0,0 ,0);

        Structure structure = new Structure(
                (short) StructureFormat.BLOCKS_ONLY.ordinal(),
                new Identifier("base", "test")
        );

        structure.addToBlock(BlockState.getInstance("base:grass[default]"), 0, 0, 0);
        structure.addToBlock(BlockState.getInstance("base:grass[default]"), 2, 0, 0);
        structure.addToBlock(BlockState.getInstance("base:grass[default]"), 1, 1, 0);
        structure.addToBlock(BlockState.getInstance("base:grass[default]"), 1, 2, 0);
        structure.addToBlock(BlockState.getInstance("base:grass[default]"), 1, 3, 0);
        structure.addToBlock(BlockState.getInstance("base:grass[default]"), 1, 4, 0);

        Structure structure2 = new Structure(
                (short) StructureFormat.BLOCKS_ONLY.ordinal(),
                new Identifier("base", "test")
        );

        structure2.addToBlock(BlockState.getInstance("base:water[default]"), 0, 0, 0);
        structure2.addToBlock(BlockState.getInstance("base:water[default]"), 2, 0, 0);
        structure2.addToBlock(BlockState.getInstance("base:water[default]"), 1, 1, 0);
        structure2.addToBlock(BlockState.getInstance("base:water[default]"), 1, 2, 0);
        structure2.addToBlock(BlockState.getInstance("base:water[default]"), 1, 3, 0);
        structure2.addToBlock(BlockState.getInstance("base:water[default]"), 1, 4, 0);

        chunks.put(new Vec3i(0, 0, 0), structure);
        chunks.put(new Vec3i(0, 1, 0), structure2);
        Threads.runOnMainThread(() -> modelInstance = new BlockModelv2(chunks));

        hasGravity = false;

    }

    @Override
    public void hit(float amount) {

    }
}
