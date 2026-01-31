package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 皮肤管理工具类，用于处理物品皮肤相关的操作
 */
public class SkinManager {
    
    /**
     * 获取玩家当前装备的皮肤名称
     *
     * @param player 玩家
     * @param itemStack 物品堆栈
     * @return 皮肤名称
     */
    public static String getEquippedSkin(Player player, ItemStack itemStack) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        return skinsComponent.getSkinFromDataSync(itemStack);

    }
    
    /**
     * 设置玩家当前装备的皮肤
     *
     * @param player 玩家
     * @param itemStack 物品堆栈
     * @param skinName 皮肤名称
     */
    public static void setEquippedSkin(Player player, ItemStack itemStack, String skinName) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        skinsComponent.setEquippedSkin(itemStack, skinName);
        skinsComponent.setSkinInDataSync(itemStack, skinName);
        skinsComponent.syncSkinsToClient();
    }
    
    /**
     * 检查玩家是否解锁了某个皮肤
     *
     * @param player 玩家
     * @param itemStack 物品堆栈
     * @param skinName 皮肤名称
     * @return 是否解锁
     */
    public static boolean isSkinUnlocked(Player player, ItemStack itemStack, String skinName) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        return skinsComponent.isSkinUnlocked(itemStack, skinName);

    }
    
    /**
     * 解锁皮肤给玩家
     *
     * @param player 玩家
     * @param itemStack 物品堆栈
     * @param skinName 皮肤名称
     */
    public static void unlockSkin(Player player, ItemStack itemStack, String skinName) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        skinsComponent.unlockSkin(itemStack, skinName);
        skinsComponent.syncSkinsToClient();
    }
    
    /**
     * 锁定皮肤（移除解锁状态）
     *
     * @param player 玩家
     * @param itemStack 物品堆栈
     * @param skinName 皮肤名称
     */
    public static void lockSkin(Player player, ItemStack itemStack, String skinName) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        skinsComponent.lockSkin(itemStack, skinName);
        skinsComponent.syncSkinsToClient();
    }
    
    /**
     * 解锁指定物品类型的皮肤
     *
     * @param player 玩家
     * @param itemTypeName 物品类型名称
     * @param skinName 皮肤名称
     */
    public static void unlockSkinForItemType(Player player, String itemTypeName, String skinName) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        skinsComponent.unlockSkinForItemType(itemTypeName, skinName);
        skinsComponent.syncSkinsToClient();
    }
    
    /**
     * 设置指定物品类型的装备皮肤
     *
     * @param player 玩家
     * @param itemTypeName 物品类型名称
     * @param skinName 皮肤名称
     */
    public static void setEquippedSkinForItemType(Player player, String itemTypeName, String skinName) {
        PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
        skinsComponent.setEquippedSkinForItemType(itemTypeName, skinName);
        skinsComponent.syncSkinsToClient();

    }
    
    /**
     * 从物品堆栈获取物品类型名称
     *
     * @param itemStack 物品堆栈
     * @return 物品类型名称
     */
    public static String getItemTypeName(ItemStack itemStack) {
        Item item = itemStack.getItem();
        String itemId = BuiltInRegistries.ITEM.getKey(item).getPath();
        return itemId.toLowerCase();
    }
}