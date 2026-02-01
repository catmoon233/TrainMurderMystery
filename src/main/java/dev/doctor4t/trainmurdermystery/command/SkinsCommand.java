package dev.doctor4t.trainmurdermystery.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.trainmurdermystery.client.gui.screen.SkinManagementScreen;
import dev.doctor4t.trainmurdermystery.network.OpenSkinScreenPaylod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

public class SkinsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("tmm:skins")
                        .executes(context -> execute(context.getSource(), null)) // 不指定玩家，默认自己
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .requires(source -> source.hasPermission(2)) // 需要权限等级2来查看其他玩家
                                .executes(context -> execute(context.getSource(), GameProfileArgument.getGameProfiles(context, "player")))
                        )
        );
    }

    private static int execute(CommandSourceStack source, Collection<GameProfile> profiles) throws CommandSyntaxException {
        ServerPlayer sender = source.getPlayerOrException();

        if (profiles == null || profiles.isEmpty()) {
            // 未指定玩家，打开自己的皮肤管理界面
            openSkinScreen(sender);
            source.sendSuccess(() -> Component.translatable("commands.tmm.showskin.self"), false);
        } else {
            // 指定玩家，打开指定玩家的皮肤管理界面
            for (GameProfile profile : profiles) {
                UUID targetUuid = profile.getId();
                ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayer(targetUuid);
                if (targetPlayer != null) {
                    openSkinScreen(targetPlayer);
                    source.sendSuccess(() -> Component.translatable("commands.tmm.showskin.other", profile.getName()), false);
                } else {
                    source.sendFailure(Component.translatable("commands.tmm.showstats.player_not_found", profile.getName()));
                }
            }
        }
        return 1;
    }

    private static void openSkinScreen(ServerPlayer player) {
        ServerPlayNetworking.send(player, new OpenSkinScreenPaylod());
    }
}