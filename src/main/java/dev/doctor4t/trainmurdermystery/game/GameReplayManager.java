package dev.doctor4t.trainmurdermystery.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.util.ReplayDisplayUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.doctor4t.trainmurdermystery.util.ReplayDisplayUtils.buildTeamPlayerRoles;

public class GameReplayManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String REPLAY_FILE_NAME = "game_replay.json";

    private GameReplayData currentReplayData;
    private final MinecraftServer server;
    private final Map<UUID, String> playerNames; // To store player names for replay display
    private GameReplay currentReplay;

    public GameReplayManager(MinecraftServer server) {
        this.server = server;
        this.currentReplayData = new GameReplayData();
        this.playerNames = new HashMap<>();
        this.currentReplay = new GameReplay(0, GameFunctions.WinStatus.NONE, new java.util.ArrayList<>(), new java.util.ArrayList<>());
    }

public void resetReplay() {
    this.currentReplayData = new GameReplayData();
    this.playerNames.clear();
    this.currentReplay = new GameReplay(0, GameFunctions.WinStatus.NONE, new java.util.ArrayList<>(), new java.util.ArrayList<>());
    }

    private GameReplay.EventType mapEventType(GameReplayData.EventType dataEventType) {
        return switch (dataEventType) {
            case KILL -> GameReplay.EventType.PLAYER_KILL;
            case POISON -> GameReplay.EventType.PLAYER_POISONED;
            case GAME_START -> GameReplay.EventType.GAME_START;
            case GAME_END -> GameReplay.EventType.GAME_END;
            case PLAYER_JOIN -> GameReplay.EventType.PLAYER_JOIN;
            case PLAYER_LEAVE -> GameReplay.EventType.PLAYER_LEAVE;
            case CUSTOM_MESSAGE, ROLE_ASSIGNMENT, ITEM_USE -> GameReplay.EventType.GAME_START; // Temporary mapping
        };
    }

    private GameReplay.ReplayEvent convertReplayEvent(GameReplayData.ReplayEvent dataEvent) {
        GameReplay.EventType eventType = mapEventType(dataEvent.getType());
        GameReplay.EventDetails details = switch (dataEvent.getType()) {
            case KILL ->
                    new GameReplay.PlayerKillDetails(dataEvent.getSourcePlayer(), dataEvent.getTargetPlayer(), ResourceLocation.parse(dataEvent.getItemUsed()));
            case POISON ->
                    new GameReplay.PlayerPoisonedDetails(dataEvent.getSourcePlayer(), dataEvent.getTargetPlayer());
            default -> new GameReplay.EventDetails() {
            }; // Generic empty details
        };

        return new GameReplay.ReplayEvent(eventType, dataEvent.getTimestamp(), details);
}

public void initializeReplay(List<ServerPlayer> players, HashMap<UUID, Role> roles) {
    resetReplay();
    for (ServerPlayer player : players) {
        recordPlayerName(player);
    }
    currentReplayData.setPlayerCount(players.size());
    // Set roles based on the provided HashMap
    currentReplayData.setCivilianPlayers(roles.entrySet().stream().filter(entry -> entry.getValue().equals(dev.doctor4t.trainmurdermystery.api.TMMRoles.CIVILIAN)).map(Map.Entry::getKey).collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll));
    currentReplayData.setKillerPlayers(roles.entrySet().stream().filter(entry -> entry.getValue().equals(dev.doctor4t.trainmurdermystery.api.TMMRoles.KILLER)).map(Map.Entry::getKey).collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll));
    currentReplayData.setVigilantePlayers(roles.entrySet().stream().filter(entry -> entry.getValue().equals(dev.doctor4t.trainmurdermystery.api.TMMRoles.VIGILANTE)).map(Map.Entry::getKey).collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll));
    currentReplayData.setLooseEndPlayers(roles.entrySet().stream().filter(entry -> entry.getValue().equals(dev.doctor4t.trainmurdermystery.api.TMMRoles.LOOSE_END)).map(Map.Entry::getKey).collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll));
    
    // 填充玩家角色映射
    Map<UUID, String> roleMap = new HashMap<>();
    for (Map.Entry<UUID, Role> entry : roles.entrySet()) {
        roleMap.put(entry.getKey(), entry.getValue().identifier().toString());
    }
    currentReplayData.setPlayerRoles(roleMap);
}

public void updateRolesFromComponent(dev.doctor4t.trainmurdermystery.cca.GameWorldComponent component) {
    currentReplayData.setCivilianPlayers(component.getAllWithRole(dev.doctor4t.trainmurdermystery.api.TMMRoles.CIVILIAN));
    currentReplayData.setKillerPlayers(component.getAllWithRole(dev.doctor4t.trainmurdermystery.api.TMMRoles.KILLER));
    currentReplayData.setVigilantePlayers(component.getAllWithRole(dev.doctor4t.trainmurdermystery.api.TMMRoles.VIGILANTE));
    currentReplayData.setLooseEndPlayers(component.getAllWithRole(dev.doctor4t.trainmurdermystery.api.TMMRoles.LOOSE_END));

    Map<UUID, String> roleMap = new HashMap<>();
    for (Map.Entry<UUID, dev.doctor4t.trainmurdermystery.api.Role> entry : component.getRoles().entrySet()) {
        roleMap.put(entry.getKey(), entry.getValue().identifier().toString());
    }
    currentReplayData.setPlayerRoles(roleMap);
}

public void finalizeReplay(GameFunctions.WinStatus winStatus) {
    currentReplayData.setWinningTeam(winStatus.name()); // Assuming WinStatus enum names can be used as team names
    saveReplay();
}

public void recordPlayerName(Player player) {
    playerNames.put(player.getUUID(), player.getName().getString());
}

public Component getPlayerName(UUID uuid) {
    String name = playerNames.get(uuid);
    return name != null ? Component.literal(name) : Component.literal("未知玩家");
}

public void addEvent(GameReplayData.EventType type, UUID sourcePlayer, UUID targetPlayer, String itemUsed, String message) {
    currentReplayData.addEvent(new GameReplayData.ReplayEvent(type, sourcePlayer, targetPlayer, itemUsed, message));
}

public void recordPlayerKill(UUID killerUuid, UUID victimUuid, ResourceLocation deathReason) {
    addEvent(GameReplayData.EventType.KILL, killerUuid, victimUuid, deathReason.toString(), null);
}

public void setPlayerCount(int count) {
    currentReplayData.setPlayerCount(count);
}

public void setCivilianPlayers(java.util.List<UUID> players) {
    currentReplayData.setCivilianPlayers(players);
}

public void setKillerPlayers(java.util.List<UUID> players) {
    currentReplayData.setKillerPlayers(players);
}

public void setVigilantePlayers(java.util.List<UUID> players) {
    currentReplayData.setVigilantePlayers(players);
}

public void setLooseEndPlayers(java.util.List<UUID> players) {
    currentReplayData.setLooseEndPlayers(players);
}

public void setWinningPlayer(UUID player) {
    currentReplayData.setWinningPlayer(player);
}

public void setWinningTeam(String team) {
    currentReplayData.setWinningTeam(team);
}

public GameReplay getCurrentReplay() {
    return currentReplay;
}

public void saveReplay() {
    File replayFile = new File(server.getServerDirectory().toFile(), REPLAY_FILE_NAME);
    try (FileWriter writer = new FileWriter(replayFile)) {
        GSON.toJson(currentReplayData, writer);
        TMM.LOGGER.info("Game replay saved to {}", replayFile.getAbsolutePath());
    } catch (IOException e) {
        TMM.LOGGER.error("Failed to save game replay", e);
    }
}

public GameReplayData loadReplay() {
    File replayFile = new File(server.getServerDirectory().toFile(), REPLAY_FILE_NAME);
    if (!replayFile.exists()) {
        TMM.LOGGER.warn("No previous game replay found.");
        return null;
    }
    try (FileReader reader = new FileReader(replayFile)) {
        GameReplayData loadedData = GSON.fromJson(reader, GameReplayData.class);
        TMM.LOGGER.info("Game replay loaded from {}", replayFile.getAbsolutePath());
        return loadedData;
    } catch (IOException e) {
        TMM.LOGGER.error("Failed to load game replay", e);
        return null;
    }
}
    public void showReplayToPlayer(ServerPlayer player) {
        GameReplayData replayData = currentReplayData;
        if (replayData == null) {
            replayData = loadReplay();
        }
        if (replayData == null) {
            player.sendSystemMessage(Component.translatable("tmm.replay.error.no_data").withStyle(ChatFormatting.RED));
            return;
        }
        // Clear previous messages
        for (int i = 0; i < 50; i++) {
            player.sendSystemMessage(Component.literal(""));
        }
        // Send game statistics
        player.sendSystemMessage(Component.translatable("tmm.replay.header").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("tmm.replay.player_count", replayData.getPlayerCount()).withStyle(ChatFormatting.WHITE));
        
        player.sendSystemMessage(Component.literal("---").withStyle(ChatFormatting.GRAY));

        Map<UUID, String> playerRoles = replayData.getPlayerRoles();
        if (playerRoles != null && !playerRoles.isEmpty()) {

            List<UUID> deadPlayers = getDeadPlayers(replayData);
            List<UUID> alivePlayers = new java.util.ArrayList<>();
            for (UUID uuid : playerRoles.keySet()) {
                if (!deadPlayers.contains(uuid)) {
                    alivePlayers.add(uuid);
                }
            }

            MutableComponent aliveText = buildTeamPlayerRoles(this, alivePlayers, playerRoles, "存活玩家：");
            MutableComponent deadText = buildTeamPlayerRoles(this, deadPlayers, playerRoles, "死亡玩家：");

            if (aliveText != null) {
                player.sendSystemMessage(aliveText.withStyle(ChatFormatting.GREEN));
            }
            if (deadText != null) {
                player.sendSystemMessage(deadText.withStyle(ChatFormatting.RED));
            }
        }
        
        player.sendSystemMessage(Component.literal("---").withStyle(ChatFormatting.GRAY));
        
        // Send winning information
        if (replayData.getWinningTeam() != null) {
            player.sendSystemMessage(Component.translatable("tmm.replay.winning_team", Component.literal(replayData.getWinningTeam()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.WHITE));
        }
        
        player.sendSystemMessage(Component.literal("---").withStyle(ChatFormatting.GRAY));
        
        // Send timeline
        player.sendSystemMessage(Component.translatable("tmm.replay.timeline").withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE));
        
        long gameStartTime = ReplayDisplayUtils.findGameStartTime(replayData);
        for (GameReplayData.ReplayEvent event : replayData.getTimeline()) {
            long relativeTime = event.getTimestamp() - gameStartTime;
            String timePrefix = ReplayDisplayUtils.formatTime(relativeTime) + " ";
            Component eventText = event.toText(this, replayData);
            player.sendSystemMessage(Component.literal(timePrefix).append(eventText));
        }
        
        player.sendSystemMessage(Component.literal("---").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("tmm.replay.footer").withStyle(ChatFormatting.GRAY));
    }
    
    private List<UUID> getDeadPlayers(GameReplayData replayData) {
        List<UUID> dead = new java.util.ArrayList<>();
        for (GameReplayData.ReplayEvent event : replayData.getTimeline()) {
            if (event.getType() == GameReplayData.EventType.KILL) {
                UUID target = event.getTargetPlayer();
                if (target != null && !dead.contains(target)) {
                    dead.add(target);
                }
            }
        }
        return dead;
    }
}
