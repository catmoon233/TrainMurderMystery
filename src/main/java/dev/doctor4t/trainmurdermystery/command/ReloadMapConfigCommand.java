package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.data.ServerMapConfig;
import dev.doctor4t.trainmurdermystery.network.SyncMapConfigPayload;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadMapConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tmm:reloadMapConfig")
                .requires(source -> source.hasPermission(2))
                .executes(ReloadMapConfigCommand::reloadMapConfig));
    }

    private static int reloadMapConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            ServerMapConfig.reload(context.getSource().getServer());
            source.sendSuccess(
                    () -> Component.translatable("commands.tmm.reloadmapconfig.success")
                            .withStyle(style -> style.withColor(0x00FF00)),
                    true);
            TMM.LOGGER.info("Map config is reloaded by {}!", source.getTextName());
            for (var serverPlayer : context.getSource().getServer().getPlayerList().getPlayers()) {
                SyncMapConfigPayload.sendToPlayer(serverPlayer);
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("commands.tmm.reloadmapconfig.fail", e.getMessage()));
            TMM.LOGGER.error("Map config reload failed.", e);
            e.printStackTrace();
            return 0;
        }

    }
}