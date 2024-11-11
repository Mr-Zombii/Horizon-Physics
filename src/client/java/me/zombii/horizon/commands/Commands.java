package me.zombii.horizon.commands;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.github.puzzle.game.util.BlockUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.InGame;

public class Commands {

    public static void register() {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("setBlock");
        cmd.then(CommandManager.argument(ServerCommandSource.class, "x", IntegerArgumentType.integer())
                .then(CommandManager.argument(ServerCommandSource.class, "y", IntegerArgumentType.integer())
                        .then(CommandManager.argument(ServerCommandSource.class, "z", IntegerArgumentType.integer())
                                .then(CommandManager.argument(ServerCommandSource.class, "blockstate", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            int x = IntegerArgumentType.getInteger(context, "x");
                                            int y = IntegerArgumentType.getInteger(context, "y");
                                            int z = IntegerArgumentType.getInteger(context, "z");
                                            String blockState = StringArgumentType.getString(context, "blockstate");

                                            BlockUtil.setBlockAt(InGame.getLocalPlayer().getZone(), BlockState.getInstance(blockState), x, y, z);
                                            return 0;
                                        })
                                )
                        )
                )
        );
        LiteralArgumentBuilder<ServerCommandSource> cmd2 = CommandManager.literal("d");
        cmd2.executes((c) -> {
            try {
                CommandManager.DISPATCHER.execute("/summon funni-blocks:entity", c.getSource());
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            return 0;
        });
        CommandManager.DISPATCHER.register(cmd);
        CommandManager.DISPATCHER.register(cmd2);
    }

}
