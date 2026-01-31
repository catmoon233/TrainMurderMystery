package dev.doctor4t.trainmurdermystery.index;


import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import dev.doctor4t.trainmurdermystery.util.WeaponSkinsSupporterData;
import dev.upcraft.datasync.api.DataSyncAPI;
import dev.upcraft.datasync.api.SyncToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.Locale;
import java.util.Optional;

import java.util.UUID;

public interface TMMCosmetics {
    // 不再重复注册，而是使用PlayerSkinsComponent中已注册的实例
    static SyncToken<WeaponSkinsSupporterData> getWeaponSkinsData() {
        return PlayerSkinsComponent.WEAPON_SKINS_DATA;
    }

    static String getSkin(ItemStack itemStack) {
        // 获取物品的owner NBT数据，如果没有则使用默认UUID
        UUID owner = UUID.fromString(itemStack.getOrDefault(TMMDataComponentTypes.OWNER, "98eaa37f-7712-4809-b709-504d3be0b6ef")); // random uuid
        String itemName = itemStack.getDescriptionId().toLowerCase(Locale.ROOT);
        Optional<WeaponSkinsSupporterData> optional = getWeaponSkinsData().get(owner);
        if (optional.isPresent()) {
            String serialized = optional.get().serialized();
            String[] namesAndSkins = serialized.split(";");
            for (String nameAndSkin : namesAndSkins) {
                if (nameAndSkin.matches(itemName + ":.+")) {
                    String[] split = nameAndSkin.split(":");
                    return split[1];
                }
            }
        }

        return "default";
    }

    static void setSkin(Player player, ItemStack itemStack, String skinName) {
        // 只有上传数据在客户端，服务器不能datasync
        if (player.level().isClientSide()) {
            StringBuilder serializedBuilder = new StringBuilder();
            Optional<WeaponSkinsSupporterData> optional = getWeaponSkinsData().get(player.getUUID());
            String itemName = itemStack.getDescriptionId().toLowerCase(Locale.ROOT);

            String[] namesAndSkins = new String[]{};
            if (optional.isPresent()) {
                namesAndSkins = optional.get().serialized().split(";");
            }

            for (String nameAndSkin : namesAndSkins) {
                if (!nameAndSkin.matches(itemName + ":.+")) {
                    serializedBuilder.append(nameAndSkin).append(";");
                }
            }

            serializedBuilder.append(itemName).append(":").append(skinName);
            String string = serializedBuilder.toString();
            WeaponSkinsSupporterData newData = new WeaponSkinsSupporterData(string);
            getWeaponSkinsData().setData(newData); // 上传到服务器
        }
    }
}