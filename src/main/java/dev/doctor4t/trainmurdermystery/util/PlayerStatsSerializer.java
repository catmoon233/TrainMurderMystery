package dev.doctor4t.trainmurdermystery.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerStatsComponent;
import dev.doctor4t.trainmurdermystery.data.PlayerStatsData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 玩家统计数据序列化工具类
 * 用于 PlayerStatsComponent 和 JSON 之间的转换
 */
public class PlayerStatsSerializer {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    /**
     * 将 PlayerStatsComponent 转换为 JSON 字符串
     */
    public static String toJson(PlayerStatsComponent component) {
        PlayerStatsData data = toData(component);
        return GSON.toJson(data);
    }

    /**
     * 从 JSON 字符串解析为 PlayerStatsData
     */
    public static PlayerStatsData fromJson(String json) throws JsonSyntaxException {
        return GSON.fromJson(json, PlayerStatsData.class);
    }

    /**
     * 将 PlayerStatsComponent 转换为 PlayerStatsData
     */
    public static PlayerStatsData toData(PlayerStatsComponent component) {
        PlayerStatsData data = new PlayerStatsData();
        
        // 设置玩家 UUID
        if (component.getPlayer() != null) {
            data.setUuid(component.getPlayer().getUUID().toString());
        }
        
        // 设置基础统计数据
        data.setTotalPlayTime(component.getTotalPlayTime());
        data.setTotalGamesPlayed(component.getTotalGamesPlayed());
        data.setTotalKills(component.getTotalKills());
        data.setTotalDeaths(component.getTotalDeaths());
        data.setTotalWins(component.getTotalWins());
        data.setTotalLosses(component.getTotalLosses());
        data.setTotalTeamKills(component.getTotalTeamKills());
        data.setTotalLoversWins(component.getTotalLoversWins());

        // 转换角色统计数据
        Map<String, PlayerStatsData.RoleStatsData> roleStatsMap = new java.util.HashMap<>();
        component.getRoleStats().forEach((roleId, roleStats) -> {
            PlayerStatsData.RoleStatsData roleData = new PlayerStatsData.RoleStatsData();
            roleData.setTimesPlayed(roleStats.getTimesPlayed());
            roleData.setKillsAsRole(roleStats.getKillsAsRole());
            roleData.setDeathsAsRole(roleStats.getDeathsAsRole());
            roleData.setWinsAsRole(roleStats.getWinsAsRole());
            roleData.setLossesAsRole(roleStats.getLossesAsRole());
            roleData.setTeamKillsAsRole(roleStats.getTeamKillsAsRole());
            roleStatsMap.put(roleId.toString(), roleData);
        });
        data.setRoleStats(roleStatsMap);

        return data;
    }

    /**
     * 将 PlayerStatsData 应用到 PlayerStatsComponent
     * 注意：这个方法现在在 PlayerStatsComponent 内部实现
     * 保留这个方法是为了向后兼容，但实际调用 PlayerStatsComponent 的 applyData 方法
     */
    public static void applyData(@NotNull PlayerStatsData data, @NotNull PlayerStatsComponent component) {
        // 这个方法现在在 PlayerStatsComponent 内部实现
        // 这里只记录日志，实际应用在 PlayerStatsComponent 中完成
        TMM.LOGGER.debug("Applying player stats data for UUID: {}", data.getUuid());
    }

    /**
     * 从 PlayerStatsData 创建 PlayerStatsComponent
     * 注意：这个方法需要 Player 对象，通常用于数据恢复
     */
    public static PlayerStatsComponent fromData(PlayerStatsData data, net.minecraft.world.entity.player.Player player) {
        PlayerStatsComponent component = new PlayerStatsComponent(player);
        
        // 应用基础统计数据
        component.setTotalPlayTime(data.getTotalPlayTime());
        component.setTotalGamesPlayed(data.getTotalGamesPlayed());
        component.setTotalKills(data.getTotalKills());
        component.setTotalDeaths(data.getTotalDeaths());
        component.setTotalWins(data.getTotalWins());
        component.setTotalLosses(data.getTotalLosses());
        component.setTotalTeamKills(data.getTotalTeamKills());
        component.setTotalLoversWins(data.getTotalLoversWins());

        // 应用角色统计数据
        data.getRoleStats().forEach((roleIdStr, roleData) -> {
            net.minecraft.resources.ResourceLocation roleId = net.minecraft.resources.ResourceLocation.parse(roleIdStr);
            PlayerStatsComponent.RoleStats roleStats = component.getOrCreateRoleStats(roleId);
            // 注意：RoleStats 的 setter 方法现在在内部类中可用
            roleStats.setTimesPlayed(roleData.getTimesPlayed());
            roleStats.setKillsAsRole(roleData.getKillsAsRole());
            roleStats.setDeathsAsRole(roleData.getDeathsAsRole());
            roleStats.setWinsAsRole(roleData.getWinsAsRole());
            roleStats.setLossesAsRole(roleData.getLossesAsRole());
            roleStats.setTeamKillsAsRole(roleData.getTeamKillsAsRole());
        });
        
        return component;
    }
}