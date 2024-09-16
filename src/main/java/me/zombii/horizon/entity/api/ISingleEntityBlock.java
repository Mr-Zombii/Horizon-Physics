package me.zombii.horizon.entity.api;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;

public interface ISingleEntityBlock {

    BlockState getState();
    void setState(BlockState state);

    static <T extends Entity & ISingleEntityBlock> void read(T entity, CRBinDeserializer deserial) {
        entity.setState(IPhysicEntity.readOrDefault(() -> {
            return BlockState.getInstance(deserial.readString("blockState"));
        }, BlockState.getInstance("base:grass[default]")));
    }

    static <T extends Entity & ISingleEntityBlock> void write(T entity, CRBinSerializer serial) {
        serial.writeString("blockState", entity.getState().getSaveKey());
    }

}
