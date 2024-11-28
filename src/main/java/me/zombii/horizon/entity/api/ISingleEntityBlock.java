package me.zombii.horizon.entity.api;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;

public interface ISingleEntityBlock {

    BlockState getState();
    void setState(BlockState state);

    static <T extends Entity & ISingleEntityBlock> void read(T entity, CRBinDeserializer deserial) {
        entity.setState(IPhysicEntity.readOrDefault(() -> {
            return BlockState.getInstance(deserial.readString("blockState"), MissingBlockStateResult.MISSING_OBJECT);
        }, BlockState.getInstance("base:grass[default]", MissingBlockStateResult.MISSING_OBJECT)));
    }

    static <T extends Entity & ISingleEntityBlock> void write(T entity, CRBinSerializer serial) {
        serial.writeString("blockState", entity.getState().getSaveKey());
    }

}
