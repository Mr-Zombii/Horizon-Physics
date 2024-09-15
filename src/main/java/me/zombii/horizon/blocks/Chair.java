package me.zombii.horizon.blocks;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.game.block.IModBlock;
import com.github.puzzle.game.generators.BlockGenerator;
import com.github.puzzle.game.generators.BlockModelGenerator;
import me.zombii.horizon.Constants;

import java.util.List;

public class Chair implements IModBlock {

    Identifier id = new Identifier(Constants.MOD_ID, "chair");

    @Override
    public BlockGenerator getBlockGenerator() {
        BlockGenerator generator = new BlockGenerator(id, id.name);
        BlockGenerator.State state = generator.createBlockState("default", "horizon_physics:chair", false);
        state.stateGenerators = new String[]{"base:rotated"};
        state.dropId = id.namespace + ":" + id.name + "[default]";
        state.blockEventsId = "base:block_events_default";
        return generator;
    }

    @Override
    public List<BlockModelGenerator> getBlockModelGenerators(Identifier blockId) {
        return List.of();
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
