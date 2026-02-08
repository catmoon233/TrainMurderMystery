package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameReplayManager;
import dev.doctor4t.trainmurdermystery.mod_whitelist.server.command.ModWhitelistCommand;
import dev.doctor4t.trainmurdermystery.network.SyncMapConfigPayload;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class DecServerJoinPlayer  {


    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    public void placeNewPlayer(Connection connection, ServerPlayer serverPlayer,
            CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        if (TMM.isLobby)
            return;
        GameReplayManager.playerNames.put(serverPlayer.getUUID(), serverPlayer.getScoreboardName());
        final var gameWorldComponent = GameWorldComponent.KEY.get(serverPlayer.level());

        if (gameWorldComponent.isRunning()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(serverPlayer)) {
                // 加群组功能已换成VoiceChat事件监听(trainVoicePlugin.java)

                if (serverPlayer.level() instanceof ServerLevel serverWorld) {
                    AreasWorldComponent areas = AreasWorldComponent.KEY.get(serverWorld);
                    AreasWorldComponent.PosWithOrientation spectatorSpawnPos = areas.getSpectatorSpawnPos();
                    serverPlayer.teleportTo(serverWorld, spectatorSpawnPos.pos.x(), spectatorSpawnPos.pos.y(),
                            spectatorSpawnPos.pos.z(), spectatorSpawnPos.yaw, spectatorSpawnPos.pitch);
                    serverPlayer.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                }

            }
        } else {
            if (serverPlayer.level() instanceof ServerLevel serverWorld) {
                // serverWorld.getSharedSpawnPos();

                AreasWorldComponent areas = AreasWorldComponent.KEY.get(serverWorld);
                AreasWorldComponent.PosWithOrientation spectatorSpawnPos = areas.getSpawnPos();
                serverPlayer.teleportTo(serverWorld, spectatorSpawnPos.pos.x(), spectatorSpawnPos.pos.y(),
                        spectatorSpawnPos.pos.z(), spectatorSpawnPos.yaw, spectatorSpawnPos.pitch);
                for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                    serverPlayer.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                    serverPlayer.containerMenu.broadcastChanges();
                    serverPlayer.inventoryMenu.slotsChanged(serverPlayer.getInventory());
                }
                if (!serverPlayer.isCreative())
                    serverPlayer.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
            }
        }
        SyncMapConfigPayload.sendToPlayer(serverPlayer);
        gameWorldComponent.setSyncRole(true);
        GameWorldComponent.KEY.syncWith(serverPlayer, (ComponentProvider) serverPlayer.level());
        gameWorldComponent.setSyncRole(false);
    }

    @Inject(method = "getMaxPlayers", at = @At("HEAD"))
    private void getMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        final var maxPlayers = ModWhitelistCommand.maxPlayers;
        if (maxPlayers != 404) {
            cir.setReturnValue(maxPlayers);
        }
    }
}
