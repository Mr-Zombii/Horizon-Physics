package org.example.exmod.items;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Queue;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.puzzle.BuilderWand;
import com.github.puzzle.util.Vec3i;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.settings.ControlSettings;
import finalforeach.cosmicreach.world.BlockSetter;
import org.example.exmod.Constants;
import org.example.exmod.entity.WorldCube;
import org.example.exmod.world.Structure;
import org.example.exmod.world.StructureWorld;
import org.example.exmod.util.SchematicConverter;

public class MoonScepter implements IModItem {

    WANDMODES wandmode = WANDMODES.SELECTPOS;
    DataTagManifest tagManifest = new DataTagManifest();
    Identifier id = new Identifier(Constants.MOD_ID, "scepter");

    public static Vector3 pos1 = null;
    public static Vector3 pos2 = null;
    boolean nextPos = false;

    public MoonScepter() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/MoonSeptor-MagixLoader.png"));
    }

    public enum WANDMODES {
        SELECTPOS("select-positions"),
        CONVERT_CHUNK("conv_chunk"),
        CONVERT("convert");

        public final String mode;

        WANDMODES(String modeName){
            this.mode = modeName;
        }

        public static WANDMODES getMode(String str){
            return switch (str) {
                case "Select Positions" -> SELECTPOS;
                case "Convert" -> CONVERT;
                case "Conv Chunk" -> CONVERT_CHUNK;
                default -> SELECTPOS;
            };
        }
    }

    @Override
    public void use(ItemSlot slot, Player player) {
        if(ControlSettings.keyCrouch.isPressed()){
            //GameSingletons.openBlockEntityScreen(player, player.getZone(InGame.world), this);
            int size = WANDMODES.values().length;
            if(wandmode.ordinal() == size - 1) wandmode = WANDMODES.SELECTPOS;
            else wandmode = WANDMODES.values()[wandmode.ordinal()+1];
            Chat.MAIN_CHAT.sendMessage(InGame.world, player, null, "Mode: "+ wandmode.mode);
            return;
        }
        switch (wandmode) {
            case SELECTPOS -> {
                setBlockPos(player);
            }
            case CONVERT -> {
                StructureWorld world = SchematicConverter.structureMapFromSchematic(BuilderWand.clipBoard);
                Entity e = new WorldCube(world);

                e.setPosition(player.getPosition());
                InGame.getLocalPlayer().getZone(InGame.world).addEntity(e);
                Chat.MAIN_CHAT.sendMessage(InGame.world, player, null, "Summoned " + e.entityTypeId + " " + player.getPosition());
//                convert(player);
            }
            case CONVERT_CHUNK -> {
                convert2(player);
            }
        }
    }

    private static Vector3 FindStartingPos(Vector3 pos1, Vector3 pos2, int l, int h, int w){
        Vector3 vec = new Vector3(pos2);
        if(pos2.z > pos1.z && pos2.x < pos1.x) vec.z = pos2.z - w;
        if(pos2.z > pos1.z && pos2.x > pos1.x) {
            vec.z = pos2.z - w;
            vec.x = pos1.x - l;
        }
        if(pos2.z < pos1.z && pos2.x > pos1.x) vec.x = pos2.x - l;
        return vec;
    }

    public static int cubize(int l) {
        while (l % 16 != 0) {
            l += 1;
        }
        return l;
    }

    private void convert2(Player player) {
        BlockPosition position = BlockSelection.getBlockPositionLookingAt();
        if(position == null) return;

        Queue<BlockPosition> positions = new Queue<>();

        StructureWorld world = new StructureWorld();
        Structure structure = new Structure((short) 0, new Identifier(Constants.MOD_ID, "ea"));
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockState state = position.chunk.getBlockState(x, y, z);
                    if (state != null) {
                        structure.setBlockState(state, x, y, z);
                        positions.addLast(new BlockPosition(position.chunk, x, y, z));
                    }
                }
            }
        }
        world.putChunkAt(new Vec3i(0, 0, 0), structure);
        BlockSetter.get().replaceBlocks(position.getZone(), BlockState.getInstance("base:air[default]"), positions);

        Entity e = new WorldCube(world);
        e.setPosition(position.chunk.blockX, position.chunk.blockY, position.chunk.blockZ);
        position.getZone().addEntity(e);
        position.chunk.getMeshGroup().flagForRemeshing(true);
        Chat.MAIN_CHAT.sendMessage(InGame.world, player, null, "Summoned " + e.entityTypeId);
    }

    private void setBlockPos(Player player) {
        BlockPosition position = BlockSelection.getBlockPositionLookingAt();
        if(position == null) return;
        Vector3 vector3 = new Vector3(position.getGlobalX(), position.getGlobalY(), position.getGlobalZ());
        if(nextPos) {
            pos1 = vector3;
            nextPos = false;
            Chat.MAIN_CHAT.sendMessage(InGame.world, player, null, "Pos1: "+ pos1);
        } else {
            pos2 = vector3;
            nextPos = true;
            Chat.MAIN_CHAT.sendMessage(InGame.world, player, null, "Pos2:" + pos2);
        }
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean isTool() {
        return true;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    @Override
    public boolean isCatalogHidden() {
        return false;
    }
}
