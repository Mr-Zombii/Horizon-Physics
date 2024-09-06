package org.example.exmod.entity;

import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import java.util.HashMap;
import java.util.Map;

// /summon funni-blocks:entity

public class BlockEntity69 extends Entity {

    Map<Vec3i, String> blocks = new HashMap<>();

    public BlockEntity69() {

        Vec3i z = new Vec3i(0,0 ,0);
        blocks.put(z, "base:stone_basalt[default]");

        Threads.runOnMainThread(() -> modelInstance = new BlockModelv2(BlockState.getInstance(blocks.get(z))));

        hasGravity = false;

    }



}
