package dev.doctor4t.trainmurdermystery.index;


import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.Locale;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent.KEY;

public interface TMMCosmetics {
    // 不再重复注册，而是使用PlayerSkinsComponent中已注册的实例


    static String getSkin(ItemStack itemStack) {
        // 获取物品的owner NBT数据，如果没有则使用默认UUID
        UUID owner = UUID.fromString(itemStack.getOrDefault(TMMDataComponentTypes.OWNER, "98eaa37f-7712-4809-b709-504d3be0b6ef")); // random uuid
        String itemName = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString() ;
        AtomicReference<String> skinName = new AtomicReference<>("default");
        Minecraft.getInstance().level.players().stream()
            .filter(player -> player.getUUID().equals(owner))
            .findFirst()
            .ifPresent(player -> {
                if (KEY.get( player).getEquippedSkins().containsKey(itemName)) {
                    skinName.set(KEY.get(player).getEquippedSkins().get(itemName));
                }
            });


            return skinName.get();

    }

    static void setSkin(Player player, ItemStack itemStack, String skinName) {
        // 只有上传数据在客户端，服务器不能datasync
            final var playerSkinsComponent = KEY.get(player);
            playerSkinsComponent.getEquippedSkins().put(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString(), skinName);
            playerSkinsComponent.sync();

    }
}