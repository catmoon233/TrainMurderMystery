package dev.doctor4t.trainmurdermystery.mixin.gui;

import com.kreezcraft.localizedchat.CommonClass;
import com.kreezcraft.localizedchat.ConfigCache;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.kreezcraft.localizedchat.CommonClass.*;

@Mixin(CommonClass.class)
public class ChatMixin2 {
    @Inject(method = "doPrefix", at = @At("RETURN"), cancellable = true)
    private static void execute(Player mainPlayer, Player comparePlayer, CallbackInfoReturnable<String> cir) {
        if (TMM.isLobby) {
            return;
        }
        if (comparePlayer.isSpectator()) {
            cir.setReturnValue("§7[旁观]§r ");

        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private static void execute(ServerPlayer sender, String message, CallbackInfoReturnable<Boolean> cir) {

        if (sender == null) {
            cir.setReturnValue(false);
        } else {
            MinecraftServer server = sender.getServer();
            if (server == null) {
                cir.setReturnValue(false);
            } else {
                String var10000 = ConfigCache.angleBraceColor;
                Component senderMessage = Component
                        .literal(var10000 + "<" + ConfigCache.nameColor + playerName(sender).getString()
                                + ConfigCache.angleBraceColor + "> " + ConfigCache.defaultColor + message);
                server.getPlayerList().broadcastSystemMessage(senderMessage, (player) -> {
                    if (sender.getUUID().equals(player.getUUID())) {
                        player.sendSystemMessage(senderMessage);
                    } else {
                        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(sender.level());
                        if (!ConfigCache.opAsPlayer
                                && server.getPlayerList().getOps().get(sender.getGameProfile()) != null
                                || sender.isSpectator() || !gameWorldComponent.isRunning()) {
                            String var5 = doPrefix(sender, player);
                            return Component.literal(var5 + ConfigCache.angleBraceColor + "<" + ConfigCache.nameColor
                                    + playerName(sender).getString() + ConfigCache.angleBraceColor + "> "
                                    + ConfigCache.defaultColor + message);
                        }

                        if (compareCoordinatesDistance(sender.blockPosition(),
                                player.blockPosition()) <= (double) ConfigCache.talkRange) {
                            String var100001 = doPrefix(sender, player);
                            return Component.literal(var100001 + ConfigCache.angleBraceColor + "<"
                                    + ConfigCache.nameColor + playerName(sender).getString()
                                    + ConfigCache.angleBraceColor + "> " + ConfigCache.defaultColor + message);
                        }
                    }

                    return null;
                }, false);
                cir.setReturnValue(true);
            }
        }
        cir.cancel();
    }
}
