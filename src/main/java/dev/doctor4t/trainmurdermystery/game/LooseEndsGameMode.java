package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.TrainWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public class LooseEndsGameMode extends GameMode {
    public LooseEndsGameMode(ResourceLocation identifier) {
        super(identifier, 60, 2);
    }

    @Override
    public void initializeGame(ServerLevel serverWorld, GameWorldComponent gameWorldComponent, List<ServerPlayer> players) {
        TrainWorldComponent.KEY.get(serverWorld).setTimeOfDay(TrainWorldComponent.TimeOfDay.SUNDOWN);

        for (ServerPlayer player : players) {
            player.getInventory().clearContent();

            ItemStack derringer = new ItemStack(TMMItems.DERRINGER);
            ItemStack knife = new ItemStack(TMMItems.KNIFE);

            int cooldown = GameConstants.getInTicks(1, 0);
            ItemCooldowns itemCooldownManager = player.getCooldowns();
            itemCooldownManager.addCooldown(TMMItems.DERRINGER, cooldown);
            itemCooldownManager.addCooldown(TMMItems.KNIFE, cooldown);

            player.addItem(new ItemStack(TMMItems.CROWBAR));
            player.addItem(derringer);
            player.addItem(knife);

            gameWorldComponent.addRole(player, TMMRoles.LOOSE_END);

            ServerPlayNetworking.send(player, new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(RoleAnnouncementTexts.LOOSE_END), -1, -1));
        }
    }

    @Override
    public void tickServerGameLoop(ServerLevel serverWorld, GameWorldComponent gameWorldComponent) {
        GameFunctions.WinStatus winStatus = GameFunctions.WinStatus.NONE;

        // check if out of time
        if (!GameTimeComponent.KEY.get(serverWorld).hasTime())
            winStatus = GameFunctions.WinStatus.TIME;

        // check if last person standing in loose end
        int playersLeft = 0;
        Player lastPlayer = null;
        for (Player player : serverWorld.players()) {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                playersLeft++;
                lastPlayer = player;
            }
        }

        if (playersLeft <= 0) {
            GameFunctions.stopGame(serverWorld);
        }

        if (playersLeft == 1) {
            gameWorldComponent.setLooseEndWinner(lastPlayer.getUUID());
            winStatus = GameFunctions.WinStatus.LOOSE_END;
        }

        // game end on win and display
        if (winStatus != GameFunctions.WinStatus.NONE && gameWorldComponent.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
            GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(serverWorld.players(), winStatus);

            GameFunctions.stopGame(serverWorld);
        }
    }
}
