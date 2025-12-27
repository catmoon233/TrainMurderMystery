package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.game.GameReplayData;
import dev.doctor4t.trainmurdermystery.game.GameReplayManager;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class ReplayDisplayUtils {

    public static MutableComponent getPlayerNames(GameReplayManager replayManager, Iterable<UUID> playerUUIDs) {
        MutableComponent names = Component.empty().copy();
        boolean first = true;

        for (UUID uuid : playerUUIDs) {
            if (!first) {
                names = names.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
            names = names.append(replayManager.getPlayerName(uuid));
            first = false;
        }

        return names;
    }

    public static Component getRoleDisplayName(String roleId) {
        ResourceLocation id = ResourceLocation.tryParse(roleId);
        if (id == null) {
            return Component.literal(roleId);
        }
        String translationKey = "announcement.role." + id.getPath();
        Component translated = Component.translatable(translationKey);
        if (translated.getString().equals(translationKey)) {
            String readable = Arrays.stream(id.getPath().split("_"))
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                    .collect(Collectors.joining(" "));
            return Component.literal(readable);
        }
        return translated;
    }

    public static MutableComponent buildTeamPlayerRoles(GameReplayManager replayManager, List<UUID> teamPlayers, Map<UUID, String> playerRoles, String prefix) {
        if (teamPlayers.isEmpty()) {
            return null;
        }
        MutableComponent text = Component.empty().copy();
        text.append(Component.literal(prefix).withStyle(ChatFormatting.WHITE));
        boolean first = true;
        for (UUID uuid : teamPlayers) {
            if (!first) {
                text.append(Component.literal("、").withStyle(ChatFormatting.GRAY));
            }
            Component playerName = replayManager.getPlayerName(uuid);
            String roleId = playerRoles.get(uuid);
            Component roleName = roleId != null ? getRoleDisplayName(roleId) : Component.literal("未知职业");
            text.append(playerName).append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(roleName).append(Component.literal(")").withStyle(ChatFormatting.GRAY));
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