package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.api.replay.GameReplayManager;
import dev.doctor4t.trainmurdermystery.api.replay.GameReplayData;
import dev.doctor4t.trainmurdermystery.api.replay.ReplayEvent;
import dev.doctor4t.trainmurdermystery.api.replay.ReplayEventTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

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

    public static MutableComponent getRoleDisplayName(String roleId) {
        ResourceLocation id = ResourceLocation.tryParse(roleId);
        if (id == null) {
            return Component.literal(roleId);
        }
        String translationKey = "announcement.role." + id.getPath();
        MutableComponent translated = Component.translatable(translationKey);
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

    // 添加一个新的方法来处理带死亡状态的显示
    public static MutableComponent buildTeamPlayerRolesWithDeathStatus(GameReplayManager replayManager, List<UUID> teamPlayers, Map<UUID, String> playerRoles, String prefix, boolean isAlive) {
        if (teamPlayers.isEmpty()) {
            return null;
        }
        MutableComponent text = Component.empty().copy();
        text.append(Component.literal(prefix).withStyle(ChatFormatting.WHITE));
        boolean first = true;
        for (UUID uuid : teamPlayers) {
            if (!first) {
                text.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
            
            // 获取玩家名称和角色
            Component playerName = replayManager.getPlayerName(uuid);
            String roleId = playerRoles.get(uuid);
            Component roleName = roleId != null ? getRoleDisplayName(roleId) : Component.literal("未知职业");
            
            // 根据角色设置颜色
            ChatFormatting roleColor = getRoleColor(roleId);
            
            // 添加玩家名和角色，并标记死亡状态
            MutableComponent playerComponent = Component.empty();
            playerComponent.append(playerName.copy().withStyle(roleColor));
            
            // 添加死亡标记
            if (!isAlive) {
                playerComponent.append(Component.literal("[死亡]"));
            }
            
            playerComponent.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
                .append(roleName).append(Component.literal(")").withStyle(ChatFormatting.GRAY));
            
            text.append(playerComponent);
            first = false;
        }
        return text;
    }
    
    private static ChatFormatting getRoleColor(String roleId) {
        if (roleId == null) {
            return ChatFormatting.WHITE; // 默认颜色
        }
        final var first = TMMRoles.ROLES.values().stream().filter(role -> role.identifier().toString().equals(roleId)).findFirst();
        if (first.isPresent()){
            final var role = first.get();
            if (role.isInnocent()){
                return ChatFormatting.GREEN;
            }
            if (role.canUseKiller()){
                return ChatFormatting.RED;
            }
            if (!role.isInnocent()){
                return ChatFormatting.YELLOW;
            }
        }
        // 根据角色类型返回对应颜色
        if (roleId.equals(TMMRoles.CIVILIAN.identifier().toString()) ||
            roleId.equals(TMMRoles.DISCOVERY_CIVILIAN.identifier().toString())) {
            return ChatFormatting.BLUE; // 民兵蓝色
        } else if (roleId.equals(TMMRoles.KILLER.identifier().toString())) {
            return ChatFormatting.DARK_RED; // 杀手深红色
        } else if (roleId.equals(TMMRoles.VIGILANTE.identifier().toString())) {
            return ChatFormatting.GOLD; // 侦探金色
        } else if (roleId.equals(TMMRoles.LOOSE_END.identifier().toString())) {
            return ChatFormatting.YELLOW; // 中立黄色
        } else {
            return ChatFormatting.GRAY; // 其他角色灰色
        }
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

    public static long getStartTime(GameReplayData replayData) {
        if (replayData.getTimeline().isEmpty()) return 0;
        return replayData.getTimeline().getFirst().timestamp();
    }

    public static String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // 新增：格式化单个事件的方法
    public static MutableComponent formatEvent(GameReplayManager replayManager, ReplayEvent event) {
        if (event == null) {
            return Component.literal("Invalid event").withStyle(ChatFormatting.RED);
        }

        MutableComponent timePrefix = Component.literal("[" + formatTime(event.timestamp()) + "] ")
                .withStyle(ChatFormatting.DARK_GRAY);

        MutableComponent eventComponent = switch (event.eventType()) {
            case GAME_START -> Component.translatable("replay.event.game.start")
                    .withStyle(ChatFormatting.GREEN);
            case GAME_END -> Component.translatable("replay.event.game.end")
                    .withStyle(ChatFormatting.RED);
            case PLAYER_JOIN -> {
                UUID playerUuid = extractPlayerUuid(event.details());
                yield Component.translatable("replay.event.player.join", 
                        replayManager.getPlayerName(playerUuid))
                    .withStyle(ChatFormatting.YELLOW);
            }
            case PLAYER_LEAVE -> {
                UUID playerUuid = extractPlayerUuid(event.details());
                yield Component.translatable("replay.event.player.leave", 
                        replayManager.getPlayerName(playerUuid))
                    .withStyle(ChatFormatting.GRAY);
            }
            case PLAYER_KILL -> {
                if (event.details() instanceof ReplayEventTypes.PlayerKillDetails killDetails) {
                    MutableComponent killerComponent = killDetails.killerUuid()
                            .map(replayManager::getPlayerName)
                            .orElse(Component.literal("Unknown Killer").withStyle(ChatFormatting.GRAY));
                    MutableComponent victimComponent = replayManager.getPlayerName(killDetails.victimUuid());
                    yield Component.translatable("replay.event.player.kill", 
                            killerComponent, victimComponent)
                        .withStyle(ChatFormatting.RED);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case PLAYER_POISONED -> {
                if (event.details() instanceof ReplayEventTypes.PlayerPoisonedDetails poisonDetails) {
                    MutableComponent poisonerComponent = poisonDetails.poisonerUuid()
                            .map(replayManager::getPlayerName)
                            .orElse(Component.literal("Unknown Poisoner").withStyle(ChatFormatting.GRAY));
                    MutableComponent victimComponent = replayManager.getPlayerName(poisonDetails.victimUuid());
                    yield Component.translatable("replay.event.player.poisoned", 
                            poisonerComponent, victimComponent)
                        .withStyle(ChatFormatting.DARK_PURPLE);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case PLAYER_REVIVAL -> {
                if (event.details() instanceof ReplayEventTypes.PlayerRevivalDetails revivalDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(revivalDetails.player());
                    yield Component.translatable("replay.event.player.revival", 
                            playerComponent, getRoleDisplayName(revivalDetails.role()))
                        .withStyle(ChatFormatting.AQUA);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case TASK_COMPLETE -> {
                if (event.details() instanceof ReplayEventTypes.TaskCompleteDetails taskDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(taskDetails.playerUuid());
                    yield Component.translatable("replay.event.task.complete", 
                            playerComponent)
                        .withStyle(ChatFormatting.GREEN);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case STORE_BUY -> {
                if (event.details() instanceof ReplayEventTypes.StoreBuyDetails buyDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(buyDetails.playerUuid());
                    yield Component.translatable("replay.event.store.buy", 
                            playerComponent, String.valueOf(buyDetails.cost()))
                        .withStyle(ChatFormatting.GOLD);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case DOOR_LOCK -> {
                if (event.details() instanceof ReplayEventTypes.DoorActionDetails doorDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(doorDetails.playerUuid());
                    yield Component.translatable("replay.event.door.lock", 
                            playerComponent)
                        .withStyle(ChatFormatting.YELLOW);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case DOOR_UNLOCK -> {
                if (event.details() instanceof ReplayEventTypes.DoorActionDetails doorDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(doorDetails.playerUuid());
                    yield Component.translatable("replay.event.door.unlock", 
                            playerComponent)
                        .withStyle(ChatFormatting.YELLOW);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case DOOR_OPEN -> {
                if (event.details() instanceof ReplayEventTypes.DoorActionDetails doorDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(doorDetails.playerUuid());
                    yield Component.translatable("replay.event.door.open", 
                            playerComponent)
                        .withStyle(ChatFormatting.YELLOW);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case DOOR_CLOSE -> {
                if (event.details() instanceof ReplayEventTypes.DoorActionDetails doorDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(doorDetails.playerUuid());
                    yield Component.translatable("replay.event.door.close", 
                            playerComponent)
                        .withStyle(ChatFormatting.YELLOW);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case ARMOR_BREAK -> {
                if (event.details() instanceof ReplayEventTypes.ArmorBreakDetails armorDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(armorDetails.playerUuid());
                    yield Component.translatable("replay.event.armor.break", 
                            playerComponent)
                        .withStyle(ChatFormatting.GRAY);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case LOCKPICK_ATTEMPT -> {
                if (event.details() instanceof ReplayEventTypes.LockpickAttemptDetails lockpickDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(lockpickDetails.playerUuid());
                    String result = lockpickDetails.success() ? "成功" : "失败";
                    yield Component.translatable("replay.event.lockpick.attempt", 
                            playerComponent, Component.literal(result))
                        .withStyle(lockpickDetails.success() ? ChatFormatting.GREEN : ChatFormatting.RED);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case ITEM_USED -> {
                if (event.details() instanceof ReplayEventTypes.ItemUsedDetails itemDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(itemDetails.playerUuid());
                    yield Component.translatable("replay.event.item.used", 
                            playerComponent)
                        .withStyle(ChatFormatting.BLUE);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case MOOD_CHANGE -> {
                if (event.details() instanceof ReplayEventTypes.MoodChangeDetails moodDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(moodDetails.playerUuid());
                    yield Component.translatable("replay.event.mood.change", 
                            playerComponent, String.valueOf(moodDetails.newMood()))
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case PSYCHO_STATE_CHANGE -> {
                if (event.details() instanceof ReplayEventTypes.PsychoStateChangeDetails psychoDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(psychoDetails.playerUuid());
                    yield Component.translatable("replay.event.psycho.state.change", 
                            playerComponent, String.valueOf(psychoDetails.newState()))
                        .withStyle(ChatFormatting.DARK_PURPLE);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case BLACKOUT_START -> {
                if (event.details() instanceof ReplayEventTypes.BlackoutEventDetails blackoutDetails) {
                    yield Component.translatable("replay.event.blackout.start")
                        .withStyle(ChatFormatting.BLACK);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case BLACKOUT_END -> {
                if (event.details() instanceof ReplayEventTypes.BlackoutEventDetails blackoutDetails) {
                    yield Component.translatable("replay.event.blackout.end")
                        .withStyle(ChatFormatting.BLACK);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case GRENADE_THROWN -> {
                if (event.details() instanceof ReplayEventTypes.GrenadeThrownDetails grenadeDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(grenadeDetails.playerUuid());
                    yield Component.translatable("replay.event.grenade.thrown", 
                            playerComponent)
                        .withStyle(ChatFormatting.RED);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case CHANGE_ROLE -> {
                if (event.details() instanceof ReplayEventTypes.ChangeRoleDetails roleDetails) {
                    MutableComponent playerComponent = replayManager.getPlayerName(roleDetails.player());
                    yield Component.translatable("replay.event.role.change", 
                            playerComponent, getRoleDisplayName(roleDetails.newRole()))
                        .withStyle(ChatFormatting.YELLOW);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
            case CUSTOM_EVENT -> {
                if (event.details() instanceof ReplayEventTypes.CustomEventDetails customDetails) {
                    yield Component.translatable("replay.event.custom.event", 
                            Component.literal(customDetails.eventId().toString()))
                        .withStyle(ChatFormatting.WHITE);
                }
                yield Component.translatable("replay.event.unknown").withStyle(ChatFormatting.RED);
            }
        };

        return timePrefix.append(eventComponent);
    }

    // 辅助方法：从事件详情中提取玩家UUID
    private static UUID extractPlayerUuid(ReplayEventTypes.EventDetails details) {
        if (details instanceof ReplayEventTypes.PlayerKillDetails killDetails) {
            return killDetails.victimUuid();
        } else if (details instanceof ReplayEventTypes.PlayerPoisonedDetails poisonDetails) {
            return poisonDetails.victimUuid();
        } else if (details instanceof ReplayEventTypes.PlayerRevivalDetails revivalDetails) {
            return revivalDetails.player();
        } else if (details instanceof ReplayEventTypes.TaskCompleteDetails taskDetails) {
            return taskDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.StoreBuyDetails buyDetails) {
            return buyDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.DoorActionDetails doorDetails) {
            return doorDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.ArmorBreakDetails armorDetails) {
            return armorDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.LockpickAttemptDetails lockpickDetails) {
            return lockpickDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.ItemUsedDetails itemDetails) {
            return itemDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.MoodChangeDetails moodDetails) {
            return moodDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.PsychoStateChangeDetails psychoDetails) {
            return psychoDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.GrenadeThrownDetails grenadeDetails) {
            return grenadeDetails.playerUuid();
        } else if (details instanceof ReplayEventTypes.ChangeRoleDetails roleDetails) {
            return roleDetails.player();
        }
        return UUID.randomUUID(); // 返回随机UUID作为后备
    }

    public static List<MutableComponent> formatAllEvents(GameReplayManager replayManager) {
        GameReplayData replayData = replayManager.currentReplayData;
        List<MutableComponent> formattedEvents = new ArrayList<>();
        for (ReplayEvent event : replayData.getTimeline()) {
            formattedEvents.add(formatEvent(replayManager, event));
        }
        return formattedEvents;
    }

    public static long getStartTime(GameReplayManager replayManager) {
        GameReplayData replayData = replayManager.currentReplayData;
        if (replayData.getTimeline().isEmpty()) return 0;
        return replayData.getTimeline().getFirst().timestamp();
    }
}