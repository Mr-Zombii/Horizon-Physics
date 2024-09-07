package org.example.exmod.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
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

    public Map<Vec3i, Structure> chunks = new HashMap<>();

    public BlockEntity69() {
        Structure structure = new Structure(
                (short) StructureFormat.BLOCKS_ONLY.ordinal(),
                new Identifier("base", "test")
        );

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    structure.addToBlock(BlockState.getInstance("base:grass[default]"), x, y, z);
                }
            }
        }

//        BlockState grass = BlockState.getInstance("base:grass[default]");
//        structure.addToBlock(grass, 0, 0, 0);
//        structure.addToBlock(grass, 2, 0, 0);
//        structure.addToBlock(grass, 1, 1, 0);
//        structure.addToBlock(grass, 1, 2, 0);
//        structure.addToBlock(grass, 1, 3, 0);
//        structure.addToBlock(grass, 1, 4, 0);
//
//        BlockState c4 = BlockState.getInstance("base:c4[default]");
//        structure.addToBlock(c4, 0, 0, 15);
//        structure.addToBlock(c4, 2, 0, 15);
//        structure.addToBlock(c4, 1, 1, 15);
//        structure.addToBlock(c4, 1, 2, 15);
//        structure.addToBlock(c4, 1, 3, 15);
//        structure.addToBlock(c4, 1, 4, 15);

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 5; z++) {
                    chunks.put(new Vec3i(x, y, z), structure);
                }
            }
        }

        Threads.runOnMainThread(() -> modelInstance = new BlockModelv2(this, chunks));

        hasGravity = false;

    }

    @Override
    public void getBoundingBox(BoundingBox boundingBox) {
        super.getBoundingBox(boundingBox);
//        boundingBox.set(this.localBoundingBox);
//
//        int max_x = 0, max_y = 0, max_z = 0;
//        int min_x = 0, min_y = 0, min_z = 0;
//
//        if (chunks == null) { return; }
//        for (Vec3i pos : chunks.keySet()) {
//            max_x = (16 * (pos.x() + 1)) > max_x ? (16 * (pos.x() + 1)) : max_x;
//            max_y = (16 * (pos.y() + 1)) > max_y ? (16 * (pos.y() + 1)) : max_y;
//            max_z = (16 * (pos.z() + 1)) > max_z ? (16 * (pos.z() + 1)) : max_z;
//
//            min_x = (16 * pos.x()) < min_x ? (16 * pos.x()) : min_x;
//            min_y = (16 * pos.y()) < min_y ? (16 * pos.y()) : min_y;
//            min_z = (16 * pos.z()) < min_z ? (16 * pos.z()) : min_z;
//        }
//        localBoundingBox.min.set(new Vector3(100, 100, 100).add(position));
//        localBoundingBox.min.set(new Vector3(min_x, min_y, min_z).add(position));
//        boundingBox.min.set(new Vector3(min_x, min_y, min_z).add(position));

//        localBoundingBox.max.set(new Vector3(max_x, max_y, max_z).add(position));
//        localBoundingBox.min.set(position);
//        boundingBox.max.set(new Vector3(max_x, max_y, max_z).add(position));
//        boundingBox.set(localBoundingBox);
//        boundingBox.update();
    }

    @Override
    public void hit(float amount) {

    }
}
