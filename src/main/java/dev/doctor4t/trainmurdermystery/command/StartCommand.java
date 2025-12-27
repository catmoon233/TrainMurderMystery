package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.api.TMMGameModes;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.command.argument.GameModeArgumentType;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StartCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:start")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("gameMode", GameModeArgumentType.gameMode())
                                .then(Commands.argument("startTimeInMinutes", IntegerArgumentType.integer(1))
                                        .executes(context -> execute(context.getSource(), GameModeArgumentType.getGameModeArgument(context, "gameMode"), IntegerArgumentType.getInteger(context, "startTimeInMinutes")))
                                )
                                .executes(context -> {
                                            GameMode gameMode = GameModeArgumentType.getGameModeArgument(context, "gameMode");
                                            return execute(context.getSource(), gameMode, -1);
                                        }
                                )
                        )
        );
    }

    private static int execute(CommandSourceStack source, GameMode gameMode, int minutes) {
        if (GameWorldComponent.KEY.get(source.getLevel()).isRunning()) {
            source.sendFailure(Component.translatable("game.start_error.game_running"));
            return -1;
        }
        if (gameMode == TMMGameModes.LOOSE_ENDS || gameMode == TMMGameModes.DISCOVERY) {
            return TMM.executeSupporterCommand(source, () -> {
                GameFunctions.startGame(source.getLevel(), gameMode, GameConstants.getInTicks(minutes >= 0 ? minutes : gameMode.defaultStartTime, 0));
                source.sendSuccess(
                        () -> Component.translatable("commands.tmm.start", gameMode.toString(), minutes)
                                .withStyle(style -> style.withColor(0x00FF00)),
                        true
                );
            });
        } else {
            GameFunctions.startGame(source.getLevel(), gameMode, GameConstants.getInTicks(minutes >= 0 ? minutes : gameMode.defaultStartTime, 0));
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.start", gameMode.toString(), minutes)
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true
            );
            return 1;
        }
    }
}