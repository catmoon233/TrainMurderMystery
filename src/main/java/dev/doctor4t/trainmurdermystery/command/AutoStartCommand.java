package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.AutoStartComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AutoStartCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:autoStart")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("seconds", IntegerArgumentType.integer(0, 60))
                                        .executes(context -> setAutoStart(context.getSource(), IntegerArgumentType.getInteger(context, "seconds")))
                        )
        );
    }

    private static int setAutoStart(CommandSourceStack source, int seconds) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    AutoStartComponent.KEY.get(source.getLevel()).setStartTime(GameConstants.getInTicks(0, seconds));
                    if (seconds > 0) {
                        source.sendSuccess(
                            () -> Component.translatable("commands.tmm.autostart.enabled", seconds)
                                .withStyle(style -> style.withColor(0x00FF00)),
                            true
                        );
                    } else {
                        source.sendSuccess(
                            () -> Component.translatable("commands.tmm.autostart.disabled")
                                .withStyle(style -> style.withColor(0x00FF00)),
                            true
                        );
                    }
                }
        );
    }
}
