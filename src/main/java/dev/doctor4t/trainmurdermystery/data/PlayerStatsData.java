package dev.doctor4t.trainmurdermystery.data;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家统计数据的数据传输对象 (DTO)
 * 用于 JSON 序列化和反序列化
 */
public class PlayerStatsData {
    private String uuid;
    private long totalPlayTime;
    private int totalGamesPlayed;
    private int totalKills;
    private int totalDeaths;
    private int totalWins;
    private int totalLosses;
    private int totalTeamKills;
    private int totalLoversWins;
    private Map<String, RoleStatsData> roleStats = new HashMap<>();

    /**
     * 角色统计数据
     */
    public static class RoleStatsData {
        private int timesPlayed;
        private int killsAsRole;
        private int deathsAsRole;
        private int winsAsRole;
        private int lossesAsRole;
        private int teamKillsAsRole;

        // 默认构造函数用于 Gson
        public RoleStatsData() {}

        // Getter 和 Setter 方法
        public int getTimesPlayed() {
            return timesPlayed;
        }

        public void setTimesPlayed(int timesPlayed) {
            this.timesPlayed = timesPlayed;
        }

        public int getKillsAsRole() {
            return killsAsRole;
        }

        public void setKillsAsRole(int killsAsRole) {
            this.killsAsRole = killsAsRole;
        }

        public int getDeathsAsRole() {
            return deathsAsRole;
        }

        public void setDeathsAsRole(int deathsAsRole) {
            this.deathsAsRole = deathsAsRole;
        }

        public int getWinsAsRole() {
            return winsAsRole;
        }

        public void setWinsAsRole(int winsAsRole) {
            this.winsAsRole = winsAsRole;
        }

        public int getLossesAsRole() {
            return lossesAsRole;
        }

        public void setLossesAsRole(int lossesAsRole) {
            this.lossesAsRole = lossesAsRole;
        }

        public int getTeamKillsAsRole() {
            return teamKillsAsRole;
        }

        public void setTeamKillsAsRole(int teamKillsAsRole) {
            this.teamKillsAsRole = teamKillsAsRole;
        }
    }

    // 默认构造函数用于 Gson
    public PlayerStatsData() {}

    // Getter 和 Setter 方法
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void setTotalWins(int totalWins) {
        this.totalWins = totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public void setTotalLosses(int totalLosses) {
        this.totalLosses = totalLosses;
    }

    public int getTotalTeamKills() {
        return totalTeamKills;
    }

    public void setTotalTeamKills(int totalTeamKills) {
        this.totalTeamKills = totalTeamKills;
    }

    public int getTotalLoversWins() {
        return totalLoversWins;
    }

    public void setTotalLoversWins(int totalLoversWins) {
        this.totalLoversWins = totalLoversWins;
    }

    public Map<String, RoleStatsData> getRoleStats() {
        return roleStats;
    }

    public void setRoleStats(Map<String, RoleStatsData> roleStats) {
        this.roleStats = roleStats;
    }
}