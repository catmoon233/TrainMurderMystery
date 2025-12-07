package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.api.Role;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public record GameReplay(int playerCount, GameFunctions.WinStatus winningTeam, List<ReplayPlayerInfo> players,
                         List<ReplayEvent> timelineEvents) {

    public record ReplayPlayerInfo(UUID uuid, String name, Role finalRole) {
    }

    public record ReplayEvent(EventType eventType, long timestamp, EventDetails details) {
    }

    public enum EventType {
        GAME_START,
        GAME_END,
        PLAYER_JOIN,
        PLAYER_LEAVE,
        PLAYER_KILL,
        PLAYER_POISONED,
        // Add more event types as needed
    }

    public interface EventDetails {
    }

    public record PlayerKillDetails(UUID killerUuid, UUID victimUuid, Identifier deathReason) implements EventDetails {
    }

    public record PlayerPoisonedDetails(UUID poisonerUuid, UUID victimUuid) implements EventDetails {
    }

    // Add more specific EventDetails classes for other event types
}