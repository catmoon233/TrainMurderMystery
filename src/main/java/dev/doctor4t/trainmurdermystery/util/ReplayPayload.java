package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameReplay;
import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReplayPayload(GameReplay replay) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ReplayPayload> ID = new CustomPacketPayload.Type<>(TMM.id("replay"));
    public static final StreamCodec<FriendlyByteBuf, ReplayPayload> CODEC = StreamCodec.ofMember(ReplayPayload::write, ReplayPayload::new);

    private ReplayPayload(FriendlyByteBuf buf) {
        this(readReplay(buf));
    }

    private void write(FriendlyByteBuf buf) {
        writeReplay(buf, replay);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private static GameReplay readReplay(FriendlyByteBuf buf) {
        int playerCount = buf.readInt();
        GameFunctions.WinStatus winningTeam = buf.readEnum(GameFunctions.WinStatus.class);

        int numPlayers = buf.readInt();
        List<GameReplay.ReplayPlayerInfo> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            UUID uuid = buf.readUUID();
            String name = buf.readUtf();
            ResourceLocation roleId = buf.readResourceLocation();

            Role role = TMMRoles.ROLES.stream()
                    .filter(r -> r.identifier().equals(roleId))
                    .findFirst()
                    .orElse(TMMRoles.CIVILIAN);
            players.add(new GameReplay.ReplayPlayerInfo(uuid, name, role));
        }

        int numEvents = buf.readInt();
        List<GameReplay.ReplayEvent> timelineEvents = new ArrayList<>();
        for (int i = 0; i < numEvents; i++) {
            GameReplay.EventType eventType = buf.readEnum(GameReplay.EventType.class);
            long timestamp = buf.readInt();
            GameReplay.EventDetails details = null;

            switch (eventType) {
                case PLAYER_KILL: {
                    int killerIndex = buf.readVarInt();
                    UUID killerUuid = players.get(killerIndex).uuid();
                    int victimIndex = buf.readVarInt();
                    UUID victimUuid = players.get(victimIndex).uuid();
                    ResourceLocation deathReason = buf.readResourceLocation();
                    details = new GameReplay.PlayerKillDetails(killerUuid, victimUuid, deathReason);
                    break;
                }
                case PLAYER_POISONED: {
                    int poisonerIndex = buf.readVarInt();
                    UUID poisonerUuid = players.get(poisonerIndex).uuid();
                    int victimIndex = buf.readVarInt();
                    UUID poisonedVictimUuid = players.get(victimIndex).uuid();
                    details = new GameReplay.PlayerPoisonedDetails(poisonerUuid, poisonedVictimUuid);
                    break;
                }
                // Add more cases for other event types
            }
            timelineEvents.add(new GameReplay.ReplayEvent(eventType, timestamp, details));
        }
        return new GameReplay(playerCount, winningTeam, players, timelineEvents);
    }

    private static void writeReplay(FriendlyByteBuf buf, GameReplay replay) {
        buf.writeInt(replay.playerCount());
        buf.writeEnum(replay.winningTeam());

        buf.writeInt(replay.players().size());
        for (GameReplay.ReplayPlayerInfo playerInfo : replay.players()) {
            buf.writeUUID(playerInfo.uuid());
            buf.writeUtf(playerInfo.name());
            buf.writeResourceLocation(playerInfo.finalRole().identifier());
        }


        Map<UUID, Integer> playerUuidToIndex = new HashMap<>();
        for (int i = 0; i < replay.players().size(); i++) {
            playerUuidToIndex.put(replay.players().get(i).uuid(), i);
        }

        buf.writeInt(replay.timelineEvents().size());
        for (GameReplay.ReplayEvent event : replay.timelineEvents()) {
            buf.writeEnum(event.eventType());
            buf.writeInt((int) event.timestamp());

            switch (event.eventType()) {
                case PLAYER_KILL:
                    GameReplay.PlayerKillDetails killDetails = (GameReplay.PlayerKillDetails) event.details();
                    buf.writeVarInt(playerUuidToIndex.get(killDetails.killerUuid()));
                    buf.writeVarInt(playerUuidToIndex.get(killDetails.victimUuid()));
                    buf.writeResourceLocation(killDetails.deathReason());
                    break;
                case PLAYER_POISONED:
                    GameReplay.PlayerPoisonedDetails poisonedDetails = (GameReplay.PlayerPoisonedDetails) event.details();
                    buf.writeVarInt(playerUuidToIndex.get(poisonedDetails.poisonerUuid()));
                    buf.writeVarInt(playerUuidToIndex.get(poisonedDetails.victimUuid()));
                    break;
                // Add more cases for other event types
            }
        }
    }
}