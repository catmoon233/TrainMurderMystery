package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.TrainWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class SetTimerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("tmm:setTimer")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(
                                CommandManager.argument("minutes", IntegerArgumentType.integer(0, 240))
                                        .then(
                                                CommandManager.argument("seconds", IntegerArgumentType.integer(0, 59))
                                                        .executes(context -> setTimer(context.getSource(), IntegerArgumentType.getInteger(context, "minutes"), IntegerArgumentType.getInteger(context, "seconds")))
                                        )
                        )
        );
    }

    private static int setTimer(ServerCommandSource source, int minutes, int seconds) {
        GameTimeComponent.KEY.get(source.getWorld()).setTime(GameConstants.getInTicks(minutes, seconds));
        return 1;
    }
}
