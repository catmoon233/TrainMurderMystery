package dev.doctor4t.trainmurdermystery.example;

import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 皮肤同步系统使用示例
 * 展示如何在游戏中集成和使用 TCP 同步功能
 */
public class SkinSyncExample {
    private static final Logger logger = LoggerFactory.getLogger(SkinSyncExample.class);
    
    /**
     * 示例 1: 玩家加入时初始化同步客户端
     */
//    public static void onPlayerJoin(Player player) {
//        try {
//            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
//
//            // 初始化同步客户端，连接到 TCP 服务器
//            // 替换 "127.0.0.1" 和 8888 为实际的服务器地址和端口
//            skinsComponent.initializeSyncClient("127.0.0.1", 9999);
//
//            logger.info("玩家 {} 的皮肤同步客户端已初始化", player.getName().getString());
//        } catch (Exception e) {
//            logger.error("初始化皮肤同步失败", e);
//        }
//    }
//
//    /**
//     * 示例 2: 玩家离开时断开同步客户端
//     */
//    public static void onPlayerQuit(Player player) {
//        try {
//            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
//            skinsComponent.disconnectSyncClient();
//
//            logger.info("玩家 {} 的皮肤同步客户端已断开", player.getName().getString());
//        } catch (Exception e) {
//            logger.error("断开皮肤同步失败", e);
//        }
//    }
    
    /**
     * 示例 3: 解锁皮肤（会自动同步到服务器）
     */
    public static void unlockWeaponSkin(Player player, String weaponType, String skinName) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            
            // 解锁皮肤
            skinsComponent.unlockSkinForItemType(weaponType, skinName);
            
            logger.info("玩家 {} 解锁了 {} 的皮肤: {}", 
                player.getName().getString(), weaponType, skinName);
        } catch (Exception e) {
            logger.error("解锁皮肤失败", e);
        }
    }
    
    /**
     * 示例 4: 设置当前装备的皮肤（会自动同步到服务器）
     */
    public static void equipSkin(Player player, String weaponType, String skinName) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            
            // 检查皮肤是否已解锁
            if (skinsComponent.isSkinUnlockedForItemType(weaponType, skinName)) {
                skinsComponent.setEquippedSkinForItemType(weaponType, skinName);
                logger.info("玩家 {} 装备了 {} 的皮肤: {}", 
                    player.getName().getString(), weaponType, skinName);
            } else {
                logger.warn("玩家 {} 尝试装备未解锁的皮肤: {}", 
                    player.getName().getString(), skinName);
            }
        } catch (Exception e) {
            logger.error("装备皮肤失败", e);
        }
    }
    
    /**
     * 示例 5: 锁定皮肤（移除解锁状态，会自动同步到服务器）
     */
    public static void lockSkin(Player player, String weaponType, String skinName) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            skinsComponent.lockSkinForItemType(weaponType, skinName);
            
            logger.info("玩家 {} 的皮肤已锁定: {}", 
                player.getName().getString(), skinName);
        } catch (Exception e) {
            logger.error("锁定皮肤失败", e);
        }
    }
    
    /**
     * 示例 6: 手动从服务器更新皮肤数据
     */
//    public static void syncSkinDataFromServer(Player player) {
//        try {
//            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
//
//            // 手动触发从服务器的数据同步
//            skinsComponent.updateSkinDataFromServer();
//
//            logger.info("玩家 {} 的皮肤数据已从服务器更新", player.getName().getString());
//        } catch (Exception e) {
//            logger.error("同步皮肤数据失败", e);
//        }
//    }
//
    /**
     * 示例 7: 获取玩家的所有解锁皮肤
     */
    public static void listUnlockedSkins(Player player, String weaponType) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            
            var unlockedSkins = skinsComponent.getUnlockedSkinsForItemType(weaponType);
            
            if (unlockedSkins.isEmpty()) {
                logger.info("玩家 {} 还没有解锁 {} 的任何皮肤", 
                    player.getName().getString(), weaponType);
            } else {
                logger.info("玩家 {} 的 {} 解锁皮肤列表:", 
                    player.getName().getString(), weaponType);
                for (String skinName : unlockedSkins.keySet()) {
                    logger.info("  - {}", skinName);
                }
            }
        } catch (Exception e) {
            logger.error("获取解锁皮肤列表失败", e);
        }
    }
    
    /**
     * 示例 8: 检查特定皮肤是否已解锁
     */
    public static boolean isSkinUnlocked(Player player, String weaponType, String skinName) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            return skinsComponent.isSkinUnlockedForItemType(weaponType, skinName);
        } catch (Exception e) {
            logger.error("检查皮肤状态失败", e);
            return false;
        }
    }
    
    /**
     * 示例 9: 获取玩家当前装备的皮肤
     */
    public static String getEquippedSkin(Player player, String weaponType) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            return skinsComponent.getEquippedSkinForItemType(weaponType);
        } catch (Exception e) {
            logger.error("获取装备皮肤失败", e);
            return "default";
        }
    }
    
    /**
     * 示例 10: 完整的皮肤切换流程
     */
    public static void switchSkin(Player player, String weaponType, String newSkinName) {
        try {
            PlayerSkinsComponent skinsComponent = PlayerSkinsComponent.KEY.get(player);
            
            // 检查新皮肤是否已解锁
            if (!skinsComponent.isSkinUnlockedForItemType(weaponType, newSkinName)) {
                logger.warn("皮肤 {} 未解锁，无法切换", newSkinName);
                return;
            }
            
            // 获取当前装备的皮肤
            String currentSkin = skinsComponent.getEquippedSkinForItemType(weaponType);
            
            // 如果是同一个皮肤，无需切换
            if (currentSkin.equals(newSkinName)) {
                logger.info("皮肤 {} 已是当前装备，无需切换", newSkinName);
                return;
            }
            
            // 切换皮肤
            skinsComponent.setEquippedSkinForItemType(weaponType, newSkinName);
            logger.info("玩家 {} 已将 {} 的皮肤从 {} 切换为 {}", 
                player.getName().getString(), weaponType, currentSkin, newSkinName);
            
        } catch (Exception e) {
            logger.error("切换皮肤失败", e);
        }
    }
    
    /**
     * 示例 11: 在命令中使用
     * 命令示例: /tmm_skin unlock diamond_sword dragon_slayer
     */
    public static void handleSkinCommand(Player player, String[] args) {
        if (args.length < 3) {
            logger.warn("用法: /tmm_skin <unlock|lock|equip|get> <weapon_type> <skin_name>");
            return;
        }
        
        String action = args[0].toLowerCase();
        String weaponType = args[1];
        String skinName = args[2];
        
        switch (action) {
            case "unlock":
                unlockWeaponSkin(player, weaponType, skinName);
                break;
            case "lock":
                lockSkin(player, weaponType, skinName);
                break;
            case "equip":
                equipSkin(player, weaponType, skinName);
                break;
            case "get":
                String equippedSkin = getEquippedSkin(player, weaponType);
                logger.info("玩家 {} 的 {} 当前装备皮肤: {}", 
                    player.getName().getString(), weaponType, equippedSkin);
                break;
            case "list":
                listUnlockedSkins(player, weaponType);
                break;
            default:
                logger.warn("未知的皮肤操作: {}", action);
        }
    }
}
