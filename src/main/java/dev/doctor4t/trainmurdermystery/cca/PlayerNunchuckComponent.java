package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家双节棍组件
 * 用于追踪玩家被双节棍击打的记录
 */
public class PlayerNunchuckComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<PlayerNunchuckComponent> KEY = ComponentRegistry.getOrCreate(
            TMM.id("player_nunchuck"), PlayerNunchuckComponent.class);

    public final Player player;
    private final Map<UUID, HitRecord> hitRecords = new HashMap<>();

    public PlayerNunchuckComponent(Player player) {
        this.player = player;
    }

    /**
     * 记录玩家被击打
     * @param attackerId 攻击者的UUID
     * @param damageType 伤害类型(0: 左, 1: 右, 2: 后)
     * @param nearBlock 是否在方块侧面被击打
     */
    public void recordHit(UUID attackerId, int damageType, boolean nearBlock) {
        HitRecord record = new HitRecord();
        record.attackerId = attackerId;
        record.damageType = damageType;
        record.nearBlock = nearBlock;
        record.lastHitTime = ((net.minecraft.server.level.ServerPlayer) player).serverLevel().getGameTime();
        record.hitCount = 1;

        // 移除旧记录
        hitRecords.remove(attackerId);
        hitRecords.put(attackerId, record);

        KEY.sync(this.player);
    }

    /**
     * 增加击打次数
     */
    public void incrementHitCount(UUID attackerId) {
        HitRecord record = hitRecords.get(attackerId);
        if (record != null) {
            record.hitCount++;
            record.lastHitTime = ((net.minecraft.server.level.ServerPlayer) player).serverLevel().getGameTime();
            KEY.sync(this.player);
        }
    }

    /**
     * 获取击打记录
     */
    public HitRecord getHitRecord(UUID attackerId) {
        return hitRecords.get(attackerId);
    }

    /**
     * 清除击打记录
     */
    public void clearHitRecord(UUID attackerId) {
        hitRecords.remove(attackerId);
        KEY.sync(this.player);
    }

    /**
     * 清除所有击打记录
     */
    public void clearAllRecords() {
        hitRecords.clear();
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        // 清理超过6秒的记录 (6秒 = 120 ticks)
        long currentTime = ((net.minecraft.server.level.ServerPlayer) player).serverLevel().getGameTime();
        long timeout = 120; // 6 seconds in ticks

        hitRecords.entrySet().removeIf(entry -> {
            HitRecord record = entry.getValue();
            if (currentTime - record.lastHitTime > timeout) {
                return true;
            }
            return false;
        });

        if (!hitRecords.isEmpty() && currentTime % 20 == 0) {
            KEY.sync(this.player);
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registryLookup) {
        CompoundTag recordsTag = new CompoundTag();
        for (Map.Entry<UUID, HitRecord> entry : hitRecords.entrySet()) {
            CompoundTag recordTag = new CompoundTag();
            recordTag.putUUID("attacker_id", entry.getValue().attackerId);
            recordTag.putInt("damage_type", entry.getValue().damageType);
            recordTag.putBoolean("near_block", entry.getValue().nearBlock);
            recordTag.putLong("last_hit_time", entry.getValue().lastHitTime);
            recordTag.putInt("hit_count", entry.getValue().hitCount);
            recordsTag.put(entry.getKey().toString(), recordTag);
        }
        tag.put("hit_records", recordsTag);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registryLookup) {
        hitRecords.clear();
        if (tag.contains("hit_records", Tag.TAG_COMPOUND)) {
            CompoundTag recordsTag = tag.getCompound("hit_records");
            for (String key : recordsTag.getAllKeys()) {
                CompoundTag recordTag = recordsTag.getCompound(key);
                HitRecord record = new HitRecord();
                record.attackerId = recordTag.getUUID("attacker_id");
                record.damageType = recordTag.getInt("damage_type");
                record.nearBlock = recordTag.getBoolean("near_block");
                record.lastHitTime = recordTag.getLong("last_hit_time");
                record.hitCount = recordTag.getInt("hit_count");
                hitRecords.put(UUID.fromString(key), record);
            }
        }
    }

    public static class HitRecord {
        public UUID attackerId;
        public int damageType; // 0: 左,1: 右, 2: 后
        public boolean nearBlock; // 是否在方块侧面
        public long lastHitTime;
        public int hitCount;
    }
}
