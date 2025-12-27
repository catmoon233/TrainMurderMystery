package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetTimerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:setTimer")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("minutes", IntegerArgumentType.integer(0, 240))
                                        .then(
                                                Commands.argument("seconds", IntegerArgumentType.integer(0, 59))
                                                        .executes(context -> setTimer(context.getSource(), IntegerArgumentType.getInteger(context, "minutes"), IntegerArgumentType.getInteger(context, "seconds")))
                                        )
                        )
        );
    }

    private static int setTimer(CommandSourceStack source, int minutes, int seconds) {
        return TMM.executeSupporterCommand(source,
                () -> {
                    GameTimeComponent.KEY.get(source.getLevel()).setTime(GameConstants.getInTicks(minutes, seconds));
                    source.sendSuccess(
                        () -> Component.translatable("commands.tmm.settimer", minutes, seconds)
                            .withStyle(style -> style.withColor(0x00FF00)),
                        true
                    );
                }
        );
    }
}
