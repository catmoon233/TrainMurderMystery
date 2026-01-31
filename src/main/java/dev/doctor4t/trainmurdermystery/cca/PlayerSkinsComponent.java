package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.upcraft.datasync.api.DataSyncAPI;
import dev.upcraft.datasync.api.SyncToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSkinsComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerSkinsComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("player_skins"), PlayerSkinsComponent.class);
    public static final ResourceLocation WEAPON_SKINS_DATA_ID = TMM.id("weapon_skins");

    private final Player player;
    private Map<String, String> equippedSkins; // 存储当前装备的皮肤 {itemName -> skinName}
    private Map<String, Map<String, Boolean>> unlockedSkins; // 存储解锁的皮肤 {itemName -> {skinName -> isUnlocked}}

    public PlayerSkinsComponent(Player player) {
        this.player = player;
        this.equippedSkins = new HashMap<>();
        this.unlockedSkins = new HashMap<>();
    }

    public void sync() {
        KEY.sync(this.player);
    }    /**
     * 获取当前装备的皮肤名称
     */
    public String getEquippedSkin(ItemStack itemStack) {
        String itemName = itemStack.getItem().toString().toLowerCase();
        return equippedSkins.getOrDefault(itemName, "default");
    }

    /**
     * 设置当前装备的皮肤名称
     */
    public void setEquippedSkin(ItemStack itemStack, String skinName) {
        String itemName = itemStack.getItem().toString().toLowerCase();
        equippedSkins.put(itemName, skinName);
    }

    /**
     * 解锁一个皮肤
     */
    public void unlockSkin(ItemStack itemStack, String skinName) {
        String itemName = itemStack.getItem().toString().toLowerCase();
        unlockedSkins.computeIfAbsent(itemName, k -> new HashMap<>()).put(skinName, true);
    }

    /**
     * 解锁指定物品类型的皮肤
     */
    public void unlockSkinForItemType(String itemTypeName, String skinName) {
        String normalizedItemName = normalizeItemName(itemTypeName);
        unlockedSkins.computeIfAbsent(normalizedItemName, k -> new HashMap<>()).put(skinName, true);
    }

    /**
     * 锁定一个皮肤（移除解锁状态）
     */
    public void lockSkin(ItemStack itemStack, String skinName) {
        String itemName = itemStack.getItem().toString().toLowerCase();
        Map<String, Boolean> skinsForItem = unlockedSkins.get(itemName);
        if (skinsForItem != null) {
            skinsForItem.remove(skinName);
            // 如果物品没有其他解锁的皮肤，移除该物品的条目
            if (skinsForItem.isEmpty()) {
                unlockedSkins.remove(itemName);
            }
        }
    }

    /**
     * 锁定指定物品类型的皮肤
     */
    public void lockSkinForItemType(String itemTypeName, String skinName) {
        String normalizedItemName = normalizeItemName(itemTypeName);
        Map<String, Boolean> skinsForItem = unlockedSkins.get(normalizedItemName);
        if (skinsForItem != null) {
            skinsForItem.remove(skinName);
            // 如果物品没有其他解锁的皮肤，移除该物品的条目
            if (skinsForItem.isEmpty()) {
                unlockedSkins.remove(normalizedItemName);
            }
        }
    }

    /**
     * 检查皮肤是否已解锁
     */
    public boolean isSkinUnlocked(ItemStack itemStack, String skinName) {
        String itemName = itemStack.getItem().toString().toLowerCase();
        Map<String, Boolean> skinsForItem = unlockedSkins.get(itemName);
        return skinsForItem != null && skinsForItem.getOrDefault(skinName, false);
    }

    /**
     * 检查指定物品类型的皮肤是否已解锁
     */
    public boolean isSkinUnlockedForItemType(String itemTypeName, String skinName) {
        String normalizedItemName = normalizeItemName(itemTypeName);
        Map<String, Boolean> skinsForItem = unlockedSkins.get(normalizedItemName);
        return skinsForItem != null && skinsForItem.getOrDefault(skinName, false);
    }

    /**
     * 获取所有解锁的皮肤
     */
    public Map<String, Boolean> getUnlockedSkins(ItemStack itemStack) {
        String itemName = itemStack.getItem().toString().toLowerCase();
        return unlockedSkins.getOrDefault(itemName, new HashMap<>());
    }

    /**
     * 获取指定物品类型的所有解锁皮肤
     */
    public Map<String, Boolean> getUnlockedSkinsForItemType(String itemTypeName) {
        String normalizedItemName = normalizeItemName(itemTypeName);
        return unlockedSkins.getOrDefault(normalizedItemName, new HashMap<>());
    }

    /**
     * 设置指定物品类型的装备皮肤
     */
    public void setEquippedSkinForItemType(String itemTypeName, String skinName) {
        String normalizedItemName = normalizeItemName(itemTypeName);
        equippedSkins.put(normalizedItemName, skinName);
    }

    /**
     * 获取指定物品类型的当前装备皮肤
     */
    public String getEquippedSkinForItemType(String itemTypeName) {
        String normalizedItemName = normalizeItemName(itemTypeName);
        return equippedSkins.getOrDefault(normalizedItemName, "default");
    }

    /**
     * 获取所有装备的皮肤映射
     */
    public Map<String, String> getEquippedSkins() {
        return new HashMap<>(this.equippedSkins);
    }

    /**
     * 获取所有解锁的皮肤映射
     */
    public Map<String, Map<String, Boolean>> getUnlockedSkins() {
        return new HashMap<>(this.unlockedSkins);
    }

    /**
     * 同步皮肤数据到客户端
     */
    public void syncSkinsToClient() {
        sync();
    }

    /**
     * 从数据同步令牌获取皮肤数据
     */
    public String getSkinFromDataSync(ItemStack itemStack) {

        // 使用物品的注册名而不是显示名称，以确保一致性
        String itemName = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        
        if (KEY.get( player).equippedSkins.containsKey(itemName)){
            return KEY.get(player).equippedSkins.get(itemName);
        }

        return "default";
    }

    /**
     * 设置数据同步中的皮肤
     */
    public void setSkinInDataSync(ItemStack itemStack, String skinName) {
        // 只在客户端上传数据
            KEY.get( player).equippedSkins.put(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString(), skinName);
            sync();

    }

    /**
     * 标准化物品名称
     */
    private String normalizeItemName(String itemTypeName) {
        // 将物品类型名称标准化为小写，去除空格等
        return itemTypeName.toLowerCase().trim().replaceAll("[^a-z0-9_:]", "");
    }


    @Override
    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        // 读取装备的皮肤数据
        CompoundTag equippedSkinsTag = compoundTag.getCompound("equippedSkins");
        for (String key : equippedSkinsTag.getAllKeys()) {
            this.equippedSkins.put(key, equippedSkinsTag.getString(key));
        }

        // 读取解锁的皮肤数据
        CompoundTag unlockedSkinsTag = compoundTag.getCompound("unlockedSkins");
        for (String itemKey : unlockedSkinsTag.getAllKeys()) {
            CompoundTag skinsForItemTag = unlockedSkinsTag.getCompound(itemKey);
            Map<String, Boolean> skinsForItem = new HashMap<>();
            for (String skinKey : skinsForItemTag.getAllKeys()) {
                skinsForItem.put(skinKey, skinsForItemTag.getBoolean(skinKey));
            }
            this.unlockedSkins.put(itemKey, skinsForItem);
        }
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        // 写入装备的皮肤数据
        CompoundTag equippedSkinsTag = new CompoundTag();
        for (Map.Entry<String, String> entry : this.equippedSkins.entrySet()) {
            equippedSkinsTag.putString(entry.getKey(), entry.getValue());
        }
        compoundTag.put("equippedSkins", equippedSkinsTag);

        // 写入解锁的皮肤数据
        CompoundTag unlockedSkinsTag = new CompoundTag();
        for (Map.Entry<String, Map<String, Boolean>> itemEntry : this.unlockedSkins.entrySet()) {
            CompoundTag skinsForItemTag = new CompoundTag();
            for (Map.Entry<String, Boolean> skinEntry : itemEntry.getValue().entrySet()) {
                skinsForItemTag.putBoolean(skinEntry.getKey(), skinEntry.getValue());
            }
            unlockedSkinsTag.put(itemEntry.getKey(), skinsForItemTag);
        }
        compoundTag.put("unlockedSkins", unlockedSkinsTag);
    }
}