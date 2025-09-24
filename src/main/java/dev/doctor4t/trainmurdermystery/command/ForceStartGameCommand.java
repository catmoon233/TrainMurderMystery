package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ForceStartGameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tmm:forceStart")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                                    GameFunctions.quickRestart(context.getSource().getWorld());
                                    return 1;
                                }
                        )

        );
    }

}
