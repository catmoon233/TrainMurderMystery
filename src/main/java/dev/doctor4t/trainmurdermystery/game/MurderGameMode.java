package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.*;
import dev.doctor4t.trainmurdermystery.event.AllowGameEnd;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class MurderGameMode extends GameMode {
    public MurderGameMode(ResourceLocation identifier) {
        super(identifier, 10, 2);
    }

    private static int assignRolesAndGetKillerCount(@NotNull ServerLevel world, @NotNull List<ServerPlayer> players,
            GameWorldComponent gameComponent) {
        // civilian base role, replaced for selected killers and vigilantes
        for (ServerPlayer player : players) {
            gameComponent.addRole(player, TMMRoles.CIVILIAN);
        }

        // select roles
        ScoreboardRoleSelectorComponent roleSelector = ScoreboardRoleSelectorComponent.KEY.get(world.getScoreboard());
        int killerCount = (int) Math.floor(players.size() / 6f);
        int total = roleSelector.assignKillers(world, gameComponent, players, killerCount);
        roleSelector.assignVigilantes(world, gameComponent, players, killerCount);
        return total;
    }

    @Override
    public void initializeGame(ServerLevel serverWorld, GameWorldComponent gameWorldComponent,
            List<ServerPlayer> players) {
        TrainWorldComponent.KEY.get(serverWorld).setTimeOfDay(TrainWorldComponent.TimeOfDay.NIGHT);

        int killerCount = assignRolesAndGetKillerCount(serverWorld, players, gameWorldComponent);

        for (ServerPlayer player : players) {
            String roleId = gameWorldComponent.isRole(player, TMMRoles.KILLER) ? TMMRoles.KILLER.identifier().toString()
                    : gameWorldComponent.isRole(player, TMMRoles.VIGILANTE) ? TMMRoles.VIGILANTE.identifier().toString()
                            : TMMRoles.CIVILIAN.identifier().toString();
            ServerPlayNetworking.send(player,
                    new AnnounceWelcomePayload(roleId, killerCount, players.size() - killerCount));
        }
    }

    @Override
    public void tickServerGameLoop(ServerLevel serverWorld, GameWorldComponent gameWorldComponent) {
        GameFunctions.WinStatus winStatus = GameFunctions.WinStatus.NONE;

        // check if out of time
        if (!GameTimeComponent.KEY.get(serverWorld).hasTime())
            winStatus = GameFunctions.WinStatus.TIME;

        boolean civilianAlive = false;
        for (ServerPlayer player : serverWorld.players()) {
            // passive money
            if (gameWorldComponent.canUseKillerFeatures(player)) {
                Integer balanceToAdd = GameConstants.PASSIVE_MONEY_TICKER.apply(serverWorld.getGameTime());
                if (balanceToAdd > 0)
                    PlayerShopComponent.KEY.get(player).addToBalance(balanceToAdd);
            }

            // check if some civilians are still alive
            if (gameWorldComponent.isInnocent(player) && !GameFunctions.isPlayerEliminated(player)) {
                civilianAlive = true;
            }
        }

        // check killer win condition (killed all civilians)
        if (!civilianAlive) {
            winStatus = GameFunctions.WinStatus.KILLERS;
        }

        // check passenger win condition (all killers are dead)
        if (winStatus == GameFunctions.WinStatus.NONE) {
            winStatus = GameFunctions.WinStatus.PASSENGERS;
            for (UUID player : gameWorldComponent.getAllKillerTeamPlayers()) {
                if (!GameFunctions.isPlayerEliminated(serverWorld.getPlayerByUUID(player))) {
                    winStatus = GameFunctions.WinStatus.NONE;
                }
            }
        }

        // 检查场上是否存在亡命徒
        if (winStatus != GameFunctions.WinStatus.NONE) {
            boolean hasLooseEndAlive = false;
            ServerPlayer lastLooseEnd = null;
            int looseEndCount = 0;

            for (ServerPlayer player : serverWorld.players()) {
                if (gameWorldComponent.isRole(player, TMMRoles.LOOSE_END)
                        && !GameFunctions.isPlayerEliminated(player)) {
                    hasLooseEndAlive = true;
                    looseEndCount++;
                    lastLooseEnd = player;
                }
            }

            // 如果只有一名亡命徒存活，且没有其他存活玩家，触发亡命徒获胜
            if (hasLooseEndAlive && looseEndCount == 1 && lastLooseEnd != null) {
                // 检查是否有其他非亡命徒的存活玩家
                boolean hasOtherAlive = false;
                for (ServerPlayer player : serverWorld.players()) {
                    if (!gameWorldComponent.isRole(player, TMMRoles.LOOSE_END)
                            && !GameFunctions.isPlayerEliminated(player)) {
                        hasOtherAlive = true;
                        break;
                    }
                }
                if (!hasOtherAlive) {
                    winStatus = GameFunctions.WinStatus.LOOSE_END;
                } else {
                    // 有其他玩家存活，游戏继续
                    winStatus = GameFunctions.WinStatus.NONE;
                }
            } else if (hasLooseEndAlive) {
                // 有多个亡命徒或其他情况，游戏继续
                winStatus = GameFunctions.WinStatus.NONE;
            }
        }

        // game end on win and display
        GameFunctions.WinStatus modifiedWinStatus = AllowGameEnd.EVENT.invoker().allowGameEnd(serverWorld,
                winStatus, false);
        if (!modifiedWinStatus.equals(GameFunctions.WinStatus.NOT_MODIFY)) {
            winStatus = modifiedWinStatus;
        }
        if (winStatus != GameFunctions.WinStatus.NONE
                && gameWorldComponent.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                GameRoundEndComponent.KEY.get(serverWorld).setRoundEndData(serverWorld.players(), winStatus);
                GameFunctions.stopGame(serverWorld);
        }
    }
}