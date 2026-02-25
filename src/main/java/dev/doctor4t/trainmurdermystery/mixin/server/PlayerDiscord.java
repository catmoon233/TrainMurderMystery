package dev.doctor4t.trainmurdermystery.mixin.server;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerDiscord {
    @Inject( method = "remove", at = @At("HEAD"))
     public void remove(ServerPlayer serverPlayer, CallbackInfo ci) {
        final var gameWorldComponent = GameWorldComponent.KEY.get(serverPlayer.level());
        if (gameWorldComponent != null && gameWorldComponent.isRunning() && GameFunctions.isPlayerAliveAndSurvival( serverPlayer)) {

            if (System.currentTimeMillis() - GameFunctions.startTime > 45000) {
                GameFunctions.killPlayer(serverPlayer, true, null, TMM.id("disconnected"));
            }
        }
    }
}
