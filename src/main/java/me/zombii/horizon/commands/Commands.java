package me.zombii.horizon.commands;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.PuzzleCommandSource;
import com.github.puzzle.game.util.BlockUtil;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import finalforeach.cosmicreach.ClientSingletons;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.gamestates.InGame;

public class Commands {

    public static void register() {
        LiteralArgumentBuilder<PuzzleCommandSource> cmd = CommandManager.literal("setBlock");
        cmd.then(CommandManager.argument("x", IntegerArgumentType.integer())
                .then(CommandManager.argument("y", IntegerArgumentType.integer())
                        .then(CommandManager.argument("z", IntegerArgumentType.integer())
                                .then(CommandManager.argument("blockstate", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            int x = IntegerArgumentType.getInteger(context, "x");
                                            int y = IntegerArgumentType.getInteger(context, "y");
                                            int z = IntegerArgumentType.getInteger(context, "z");
                                            String blockState = StringArgumentType.getString(context, "blockstate");

                                            BlockUtil.setBlockAt(InGame.getLocalPlayer().getZone(InGame.world), BlockState.getInstance(blockState), x, y, z);
                                            return 0;
                                        })
                                )
                        )
                )
        );
        LiteralArgumentBuilder<PuzzleCommandSource> cmd2 = CommandManager.literal("d");
        cmd2.executes((c) -> {
            try {
                Chat.MAIN_CHAT.sendMessageOrCommand(InGame.world, InGame.getLocalPlayer(), ClientSingletons.account, "/summon funni-blocks:entity");
            } catch (Exception e) {
                e.printStackTrace();
                return 1;
            }
            return 0;
        });
        CommandManager.dispatcher.register(cmd);
        CommandManager.dispatcher.register(cmd2);
    }

}
