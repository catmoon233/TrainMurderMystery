package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ForceStop {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tmm:forceStop")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            GameFunctions.finalizeGame(context.getSource().getWorld());
                            return 1;
                        })
        );
    }
}
