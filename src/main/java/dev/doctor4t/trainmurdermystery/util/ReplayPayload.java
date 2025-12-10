package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameReplay;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.*;

public record ReplayPayload(GameReplay replay) implements CustomPayload {
    public static final CustomPayload.Id<ReplayPayload> ID = new CustomPayload.Id<>(TMM.id("replay"));
    public static final PacketCodec<PacketByteBuf, ReplayPayload> CODEC = PacketCodec.of(ReplayPayload::write, ReplayPayload::new);

    private ReplayPayload(PacketByteBuf buf) {
        this(readReplay(buf));
    }

    private void write(PacketByteBuf buf) {
        writeReplay(buf, replay);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static GameReplay readReplay(PacketByteBuf buf) {
        int playerCount = buf.readInt();
        GameFunctions.WinStatus winningTeam = buf.readEnumConstant(GameFunctions.WinStatus.class);

        int numPlayers = buf.readInt();
        List<GameReplay.ReplayPlayerInfo> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            UUID uuid = buf.readUuid();
            String name = buf.readString();
            Identifier roleId = buf.readIdentifier();

            Role role = TMMRoles.ROLES.stream()
                    .filter(r -> r.identifier().equals(roleId))
                    .findFirst()
                    .orElse(TMMRoles.CIVILIAN);
            players.add(new GameReplay.ReplayPlayerInfo(uuid, name, role));
        }

        int numEvents = buf.readInt();
        List<GameReplay.ReplayEvent> timelineEvents = new ArrayList<>();
        for (int i = 0; i < numEvents; i++) {
            GameReplay.EventType eventType = buf.readEnumConstant(GameReplay.EventType.class);
            long timestamp = buf.readInt(); // OPTIMIZATION
            GameReplay.EventDetails details = null;

            switch (eventType) {
                case PLAYER_KILL: {
                    int killerIndex = buf.readVarInt();
                    UUID killerUuid = players.get(killerIndex).uuid();
                    int victimIndex = buf.readVarInt();
                    UUID victimUuid = players.get(victimIndex).uuid();
                    Identifier deathReason = buf.readIdentifier();
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

    private static void writeReplay(PacketByteBuf buf, GameReplay replay) {
        buf.writeInt(replay.playerCount());
        buf.writeEnumConstant(replay.winningTeam());

        buf.writeInt(replay.players().size());
        for (GameReplay.ReplayPlayerInfo playerInfo : replay.players()) {
            buf.writeUuid(playerInfo.uuid());
            buf.writeString(playerInfo.name());
            buf.writeIdentifier(playerInfo.finalRole().identifier());
        }


        Map<UUID, Integer> playerUuidToIndex = new HashMap<>();
        for (int i = 0; i < replay.players().size(); i++) {
            playerUuidToIndex.put(replay.players().get(i).uuid(), i);
        }

        buf.writeInt(replay.timelineEvents().size());
        for (GameReplay.ReplayEvent event : replay.timelineEvents()) {
            buf.writeEnumConstant(event.eventType());
            buf.writeInt((int) event.timestamp());

            switch (event.eventType()) {
                case PLAYER_KILL:
                    GameReplay.PlayerKillDetails killDetails = (GameReplay.PlayerKillDetails) event.details();
                    buf.writeVarInt(playerUuidToIndex.get(killDetails.killerUuid()));
                    buf.writeVarInt(playerUuidToIndex.get(killDetails.victimUuid()));
                    buf.writeIdentifier(killDetails.deathReason());
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