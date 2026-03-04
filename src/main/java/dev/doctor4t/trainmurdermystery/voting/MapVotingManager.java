package dev.doctor4t.trainmurdermystery.voting;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.MapVotingComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public class MapVotingManager {
    private static MapVotingManager instance;

    private MapVotingManager() {
    }

    public static synchronized MapVotingManager getInstance() {
        if (instance == null) {
            instance = new MapVotingManager();
        }
        return instance;
    }

    public void startVoting(int votingTimeSeconds) {
        if (GameFunctions.isStartingGame) {
            TMM.LOGGER.warn("Voting start failed: Game is starting!");
            return;
        }
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(level);
            if (gameWorldComponent.isRunning()) {
                TMM.LOGGER.warn("Voting start failed: Game has already started!");
                return;
            }

            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            votingComponent.startVoting(votingTimeSeconds);
        }
    }

    public void reset() {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            votingComponent.reset();
        }
    }

    public boolean isVotingActive() {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.isVotingActive();
        }
        return false;
    }

    public boolean voteForMap(UUID playerId, String mapId) {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.voteForMap(playerId, mapId);
        }
        return false;
    }

    public String getMostVotedMap() {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.getMostVotedMap();
        }

        return "random";
    }

    public int getVoteCount(String mapId) {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.getVoteCount(mapId);
        }
        return 0;
    }

    public Map<String, Integer> getAllVotes() {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.getAllVotes();
        }
        return new java.util.HashMap<>();
    }

    public void tick() {
        // Tick 现在由 MapVotingComponent 处理
        // 但我们可以保留这个方法用于其他可能的逻辑
    }

    public int getVotingTimeLeft() {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.getVotingTimeLeft();
        }
        return 0;
    }

    public int getTotalVotingTime() {
        MinecraftServer server = TMM.SERVER;
        if (server != null) {
            Level level = server.overworld();
            MapVotingComponent votingComponent = MapVotingComponent.KEY.get(level);
            return votingComponent.getTotalVotingTime();
        }
        return 0;
    }
}