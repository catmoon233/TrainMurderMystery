package dev.doctor4t.trainmurdermystery.game;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

        private static final Map<Identifier, Item> DEATH_REASON_TO_ITEM = new HashMap<>();

        static {
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.BAT, TMMItems.BAT);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.GUN, TMMItems.REVOLVER);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.KNIFE, TMMItems.KNIFE);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.GRENADE, TMMItems.GRENADE);
            DEATH_REASON_TO_ITEM.put(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.POISON, TMMItems.POISON_VIAL);
            // 注意：FELL_OUT_OF_TRAIN 和 GENERIC 没有对应物品
        }

        private Text getItemUsedText() {
            Identifier id = Identifier.tryParse(itemUsed);
            if (id == null) {
                return Text.literal(itemUsed);
            }
            // 检查是否是死亡原因标识符
            Item item = DEATH_REASON_TO_ITEM.get(id);
            if (item != null) {
                return new ItemStack(item).toHoverableText();
            }
            // 否则，尝试作为普通物品获取
            ItemStack stack = Registries.ITEM.getOrEmpty(id)
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                return stack.toHoverableText();
            }
            // 回退到可读的标识符路径
            String path = id.getPath();
            String readable = Arrays.stream(path.split("_"))
                    .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                    .collect(Collectors.joining(" "));
            return Text.literal(readable);
        }


        public Text toText(GameReplayManager manager, GameReplayData replayData) {
            Text sourceName = sourcePlayer != null ? manager.getPlayerName(sourcePlayer) : Text.literal("未知玩家").formatted(Formatting.GRAY);
            Text targetName = targetPlayer != null ? manager.getPlayerName(targetPlayer) : Text.literal("未知玩家").formatted(Formatting.GRAY);

            return switch (type) {
                case KILL -> Text.translatable("tmm.replay.event.kill", sourceName, getItemUsedText(), targetName);
                case POISON -> Text.translatable("tmm.replay.event.poison", sourceName, getItemUsedText(), targetName);
                case CUSTOM_MESSAGE -> Text.literal(message).formatted(Formatting.WHITE);
                case GAME_START -> Text.translatable("tmm.replay.event.game_start").formatted(Formatting.GREEN);
                case GAME_END -> Text.translatable("tmm.replay.event.game_end", Text.literal(replayData.getWinningTeam()).formatted(Formatting.GOLD)).formatted(Formatting.GREEN);
                case ROLE_ASSIGNMENT -> Text.translatable("tmm.replay.event.role_assignment", targetName, Text.literal(message).formatted(Formatting.YELLOW));
                case ITEM_USE -> Text.translatable("tmm.replay.event.item_use", sourceName, getItemUsedText());
                case PLAYER_JOIN -> Text.translatable("tmm.replay.event.player_join", sourceName).formatted(Formatting.GRAY);
                case PLAYER_LEAVE -> Text.translatable("tmm.replay.event.player_leave", sourceName).formatted(Formatting.GRAY);
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