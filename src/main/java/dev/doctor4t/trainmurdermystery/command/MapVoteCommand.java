package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.data.MapConfig;
import dev.doctor4t.trainmurdermystery.network.ShowSelectedMapUIPayload;
import dev.doctor4t.trainmurdermystery.voting.MapVotingManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class MapVoteCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:votemap")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> startVoting(context.getSource(), 60 * 20)) // 默认60秒
                        .then(Commands.argument("time", IntegerArgumentType.integer(10 * 20, 300 * 20)) // 时间范围10-300秒
                                .executes(context -> startVoting(context.getSource(),
                                        IntegerArgumentType.getInteger(context, "time")))));
    }

    private static int startVoting(CommandSourceStack source, int time) {
        MapVotingManager votingManager = MapVotingManager.getInstance();

        if (votingManager.isVotingActive()) {
            source.sendFailure(Component.translatable("command.tmm.votemap.already_running"));
            return 0;
        }

        votingManager.startVoting(time);
        source.getServer().getPlayerList().getPlayers().forEach(
                serverPlayer -> {
                    ServerPlayNetworking.send(serverPlayer,
                            new ShowSelectedMapUIPayload(MapConfig.getInstance()));
                });
        source.sendSuccess(() -> Component.translatable("command.tmm.votemap.success"), false);

        return 1;
    }
}