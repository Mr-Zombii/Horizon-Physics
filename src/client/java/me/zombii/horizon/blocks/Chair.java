package me.zombii.horizon.blocks;

import com.github.puzzle.game.block.IModBlock;
import com.github.puzzle.game.block.generators.BlockGenerator;
import com.github.puzzle.game.block.generators.model.BlockModelGenerator;
import finalforeach.cosmicreach.util.Identifier;
import me.zombii.horizon.Constants;

import java.util.List;

public class Chair implements IModBlock {

    Identifier id = Identifier.of(Constants.MOD_ID, "chair");

    @Override
    public BlockGenerator getBlockGenerator() {
        BlockGenerator generator = new BlockGenerator(id);
        BlockGenerator.State state = generator.createBlockState("default", "horizon:chair", false);
        state.stateGenerators = new String[]{ "base:rotated" };
        state.dropId = id.getNamespace() + ":" + id.getName() + "[default]";
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
