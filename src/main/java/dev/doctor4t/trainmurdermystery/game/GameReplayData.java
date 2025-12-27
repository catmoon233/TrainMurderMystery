package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.index.TMMItems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GameReplayData {
    private int playerCount;
    private List<UUID> civilianPlayers;
    private List<UUID> killerPlayers;
    private List<UUID> vigilantePlayers;
    private List<UUID> looseEndPlayers;
    private UUID winningPlayer;
    private String winningTeam;
    private final List<ReplayEvent> timeline;
    private Map<UUID, String> playerRoles;

    public GameReplayData() {
        this.playerCount = 0;
        this.civilianPlayers = new ArrayList<>();
        this.killerPlayers = new ArrayList<>();
        this.vigilantePlayers = new ArrayList<>();
        this.looseEndPlayers = new ArrayList<>();
        this.timeline = new ArrayList<>();
        this.playerRoles = new HashMap<>();
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public void setCivilianPlayers(List<UUID> civilianPlayers) {
        this.civilianPlayers = civilianPlayers;
    }

    public void setKillerPlayers(List<UUID> killerPlayers) {
        this.killerPlayers = killerPlayers;
    }

    public void setVigilantePlayers(List<UUID> vigilantePlayers) {
        this.vigilantePlayers = vigilantePlayers;
    }

    public void setLooseEndPlayers(List<UUID> looseEndPlayers) {
        this.looseEndPlayers = looseEndPlayers;
    }

    public void setWinningPlayer(UUID winningPlayer) {
        this.winningPlayer = winningPlayer;
    }

    public void setWinningTeam(String winningTeam) {
        this.winningTeam = winningTeam;
    }

    public void addEvent(ReplayEvent event) {
        this.timeline.add(event);
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public List<UUID> getCivilianPlayers() {
        return civilianPlayers;
    }

    public List<UUID> getKillerPlayers() {
        return killerPlayers;
    }

    public List<UUID> getVigilantePlayers() {
        return vigilantePlayers;
    }

    public List<UUID> getLooseEndPlayers() {
        return looseEndPlayers;
    }

    public UUID getWinningPlayer() {
        return winningPlayer;
    }

    public String getWinningTeam() {
        return winningTeam;
    }

    public List<ReplayEvent> getTimeline() {
        return timeline;
    }

    public Map<UUID, String> getPlayerRoles() {
        return playerRoles;
    }

    public void setPlayerRoles(Map<UUID, String> playerRoles) {
        this.playerRoles = playerRoles;
    }

    public static class ReplayEvent {
        private final long timestamp;
        private final EventType type;
        private final UUID sourcePlayer;
        private final UUID targetPlayer;
        private final String itemUsed;
        private final String message;

        public ReplayEvent(EventType type, UUID sourcePlayer, UUID targetPlayer, String itemUsed, String message) {
            this.timestamp = System.currentTimeMillis();
            this.type = type;
            this.sourcePlayer = sourcePlayer;
            this.targetPlayer = targetPlayer;
            this.itemUsed = itemUsed;
            this.message = message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public EventType getType() {
            return type;
        }

        public UUID getSourcePlayer() {
            return sourcePlayer;
        }

        public UUID getTargetPlayer() {
            return targetPlayer;
        }

        public String getItemUsed() {
            return itemUsed;
        }

        public String getMessage() {
            return message;
        }

        private static final Map<ResourceLocation, Item> DEATH_REASON_TO_ITEM = new HashMap<>();

        static {
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.BAT, TMMItems.BAT);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.GUN, TMMItems.REVOLVER);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.KNIFE, TMMItems.KNIFE);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.GRENADE, TMMItems.GRENADE);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.POISON, TMMItems.POISON_VIAL);
            // 注意：FELL_OUT_OF_TRAIN 和 GENERIC 没有对应物品
        }

        private Component getItemUsedText() {
            ResourceLocation id = ResourceLocation.tryParse(itemUsed);
            if (id == null) {
                return Component.literal(itemUsed);
            }
            // 检查是否是死亡原因标识符
            Item item = DEATH_REASON_TO_ITEM.get(id);
            if (item != null) {
                return new ItemStack(item).getDisplayName();
            }
            // 否则，尝试作为普通物品获取
            ItemStack stack = BuiltInRegistries.ITEM.getOptional(id)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                return stack.getDisplayName();
            }
            // 回退到可读的标识符路径
            String path = id.getPath();
            String readable = Arrays.stream(path.split("_"))
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                    .collect(Collectors.joining(" "));
            return Component.literal(readable);
        }


        public Component toText(GameReplayManager manager, GameReplayData replayData) {
            Component sourceName = sourcePlayer != null ? manager.getPlayerName(sourcePlayer) : Component.literal("未知玩家").withStyle(ChatFormatting.GRAY);
            Component targetName = targetPlayer != null ? manager.getPlayerName(targetPlayer) : Component.literal("未知玩家").withStyle(ChatFormatting.GRAY);

            return switch (type) {
                case KILL -> Component.translatable("tmm.replay.event.kill", sourceName, getItemUsedText(), targetName);
                case POISON -> Component.translatable("tmm.replay.event.poison", sourceName, getItemUsedText(), targetName);
                case CUSTOM_MESSAGE -> Component.literal(message).withStyle(ChatFormatting.WHITE);
                case GAME_START -> Component.translatable("tmm.replay.event.game_start").withStyle(ChatFormatting.GREEN);
                case GAME_END -> Component.translatable("tmm.replay.event.game_end", Component.literal(replayData.getWinningTeam()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GREEN);
                case ROLE_ASSIGNMENT -> Component.translatable("tmm.replay.event.role_assignment", targetName, Component.literal(message).withStyle(ChatFormatting.YELLOW));
                case ITEM_USE -> Component.translatable("tmm.replay.event.item_use", sourceName, getItemUsedText());
                case PLAYER_JOIN -> Component.translatable("tmm.replay.event.player_join", sourceName).withStyle(ChatFormatting.GRAY);
                case PLAYER_LEAVE -> Component.translatable("tmm.replay.event.player_leave", sourceName).withStyle(ChatFormatting.GRAY);
            };
        }
    }

    public enum EventType {
        KILL,
        POISON,
        CUSTOM_MESSAGE,
        GAME_START,
        GAME_END,
        ROLE_ASSIGNMENT,
        ITEM_USE,
        PLAYER_JOIN,
        PLAYER_LEAVE
    }
}