package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PlayerAFKComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<PlayerAFKComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("afk"),
            PlayerAFKComponent.class);
    private final Player player;
    private int afkTime = 0; // 挂机时间（刻）
    private int lastActionTime = 0; // 最后操作时间（刻）
    private boolean isAFK = false; // 是否挂机

    public PlayerAFKComponent(Player player) {
        this.player = player;
        this.lastActionTime = 0;
        this.afkTime = 0;
        this.isAFK = false;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void clear() {
        reset();
    }

    @Override
    public void reset() {
        this.resetAFKTimer();
    }

    public void resetAFKTimer() {
        this.lastActionTime = 0;
        this.afkTime = 0;
        this.isAFK = false;
        this.sync();
    }

    public void updateActivity() {
        this.lastActionTime = 0;
        if (this.isAFK) {
            this.isAFK = false;
            this.afkTime = 0;
            this.sync();
        }
        if (getAFKProgress() >= 0.3) {
            this.isAFK = true;
        }

    }

    public void setAFKTime(int ticks) {
        this.afkTime = ticks;
        this.lastActionTime = ticks;
        // 根据设置的时间更新AFK状态
        int afkThreshold = TMMConfig.afkThresholdSeconds * 20; // 转换为ticks
        this.isAFK = this.afkTime >= afkThreshold;
        this.sync();
    }

    public int getAFKTime() {
        return this.afkTime;
    }

    public boolean isAFK() {
        return this.isAFK;
    }

    public boolean isSleepy() {
        int sleepyThreshold = TMMConfig.afkSleepySeconds * 20; // 转换为ticks
        return this.afkTime >= sleepyThreshold && !this.isAFK;
    }

    public boolean isWarning() {
        int warningThreshold = TMMConfig.afkWarningSeconds * 20; // 转换为ticks
        int afkThreshold = TMMConfig.afkThresholdSeconds * 20; // 转换为ticks
        return this.afkTime >= warningThreshold && this.afkTime < afkThreshold && !this.isAFK;
    }

    public float getAFKProgress() {
        int afkThreshold = TMMConfig.afkThresholdSeconds * 20; // 转换为ticks
        return (float) this.afkTime / afkThreshold;
    }

    public static int tickR = 0;

    @Override
    public void serverTick() {
        ++tickR;
        if (!TMM.isPlayerInGame(this.player))
            return;

        if (!GameWorldComponent.KEY.get(this.player.level()).isRunning())
            return;
        this.lastActionTime++;
        this.afkTime = this.lastActionTime;

        // 检查是否达到挂机阈值
        int afkThreshold = TMMConfig.afkThresholdSeconds * 20; // 转换为ticks
        int warningThreshold = TMMConfig.afkWarningSeconds * 20; // 转换为ticks
        int sleepyThreshold = TMMConfig.afkSleepySeconds * 20; // 转换为ticks
        int deathThreshold = TMMConfig.afkDeathSeconds * 20; // 添加死亡阈值，转换为ticks
        if (tickR % 200 == 0) {// 10s 同步一次
            this.sync(); // 确保客户端同步进度
        }
        if (this.lastActionTime >= deathThreshold) {
            // 如果达到死亡阈值，直接杀死玩家
            GameFunctions.killPlayer(this.player, true, null, TMM.id("death_afk"));
        } else if (this.lastActionTime >= afkThreshold && !this.isAFK) {
            this.isAFK = true;
            if (tickR % 400 == 0) { // 20s同步一次
                this.sync();
            }
        } else if (this.lastActionTime >= warningThreshold && this.lastActionTime < afkThreshold && !this.isAFK) {
            // 接近挂机阈值但还未达到
            if (tickR % 200 == 0) {// 10s 同步一次
                this.sync(); // 确保客户端同步进度
            }
        } else if (this.lastActionTime >= sleepyThreshold && this.lastActionTime < warningThreshold && !this.isAFK) {
            // 开始显示困倦效果
            if (tickR % 100 == 0) {// 5s 同步一次
                this.sync(); // 确保客户端同步进度
            }
        }
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registryLookup) {
        this.afkTime = tag.getInt("afkTime");
        this.isAFK = tag.getBoolean("isAFK");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registryLookup) {
        tag.putInt("afkTime", this.afkTime);
        tag.putBoolean("isAFK", this.isAFK);
    }

    @Override
    public void clientTick() {
        ++tickR; // 避免停滞
    }
}