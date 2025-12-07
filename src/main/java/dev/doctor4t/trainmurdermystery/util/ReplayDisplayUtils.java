package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.game.GameReplayData;
import dev.doctor4t.trainmurdermystery.game.GameReplayManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReplayDisplayUtils {

    public static MutableText getPlayerNames(GameReplayManager replayManager, Iterable<UUID> playerUUIDs) {
        MutableText names = Text.empty().copy();
        boolean first = true;

        for (UUID uuid : playerUUIDs) {
            if (!first) {
                names = names.append(Text.literal(", ").formatted(Formatting.GRAY));
            }
            names = names.append(replayManager.getPlayerName(uuid));
            first = false;
        }

        return names;
    }

    public static Text getRoleDisplayName(String roleId) {
        Identifier id = Identifier.tryParse(roleId);
        if (id == null) {
            return Text.literal(roleId);
        }
        String translationKey = "announcement.role." + id.getPath();
        Text translated = Text.translatable(translationKey);
        if (translated.getString().equals(translationKey)) {
            String readable = Arrays.stream(id.getPath().split("_"))
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                    .collect(Collectors.joining(" "));
            return Text.literal(readable);
        }
        return translated;
    }

    public static MutableText buildTeamPlayerRoles(GameReplayManager replayManager, List<UUID> teamPlayers, Map<UUID, String> playerRoles, String prefix) {
        if (teamPlayers.isEmpty()) {
            return null;
        }
        MutableText text = Text.empty().copy();
        text.append(Text.literal(prefix).formatted(Formatting.WHITE));
        boolean first = true;
        for (UUID uuid : teamPlayers) {
            if (!first) {
                text.append(Text.literal("、").formatted(Formatting.GRAY));
            }
            Text playerName = replayManager.getPlayerName(uuid);
            String roleId = playerRoles.get(uuid);
            Text roleName = roleId != null ? getRoleDisplayName(roleId) : Text.literal("未知职业");
            text.append(playerName).append(Text.literal(" (").formatted(Formatting.GRAY))
                .append(roleName).append(Text.literal(")").formatted(Formatting.GRAY));
            first = false;
        }
        return text;
    }

    public static long findGameStartTime(GameReplayData replayData) {
        for (GameReplayData.ReplayEvent event : replayData.getTimeline()) {
            if (event.getType() == GameReplayData.EventType.GAME_START) {
                return event.getTimestamp();
            }
        }
        if (!replayData.getTimeline().isEmpty()) {
            return replayData.getTimeline().getFirst().getTimestamp();
        }
        return 0;
    }

    public static String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}