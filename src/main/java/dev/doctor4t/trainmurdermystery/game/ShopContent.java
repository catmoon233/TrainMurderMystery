package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShopContent {
    public static List<ShopEntry> defaultEntries = new ArrayList<>();

    public static void register(){
        {
            defaultEntries.add(new ShopEntry(TMMItems.KNIFE.getDefaultInstance(), TMMConfig.knifePrice, ShopEntry.Type.WEAPON));
            defaultEntries.add(new ShopEntry(TMMItems.REVOLVER.getDefaultInstance(), TMMConfig.revolverPrice, ShopEntry.Type.WEAPON));
            defaultEntries.add(new ShopEntry(TMMItems.GRENADE.getDefaultInstance(), TMMConfig.grenadePrice, ShopEntry.Type.WEAPON));
            defaultEntries.add(new ShopEntry(TMMItems.PSYCHO_MODE.getDefaultInstance(), TMMConfig.psychoModePrice, ShopEntry.Type.WEAPON) {
                @Override
                public boolean onBuy(@NotNull Player player) {
                    return PlayerShopComponent.usePsychoMode(player);
                }
            });
            defaultEntries.add(new ShopEntry(TMMItems.POISON_VIAL.getDefaultInstance(), TMMConfig.poisonVialPrice, ShopEntry.Type.POISON));
            defaultEntries.add(new ShopEntry(TMMItems.SCORPION.getDefaultInstance(), TMMConfig.scorpionPrice, ShopEntry.Type.POISON));
            defaultEntries.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultInstance(), TMMConfig.firecrackerPrice, ShopEntry.Type.TOOL));
            defaultEntries.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), TMMConfig.lockpickPrice, ShopEntry.Type.TOOL));
            defaultEntries.add(new ShopEntry(TMMItems.CROWBAR.getDefaultInstance(), TMMConfig.crowbarPrice, ShopEntry.Type.TOOL));
            defaultEntries.add(new ShopEntry(TMMItems.BODY_BAG.getDefaultInstance(), TMMConfig.bodyBagPrice, ShopEntry.Type.TOOL));
            defaultEntries.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultInstance(), TMMConfig.blackoutPrice, ShopEntry.Type.TOOL) {
                @Override
                public boolean onBuy(@NotNull Player player) {
                    return PlayerShopComponent.useBlackout(player);
                }
            });
            defaultEntries.add(new ShopEntry(new ItemStack(TMMItems.NOTE, 4), TMMConfig.notePrice, ShopEntry.Type.TOOL));
        }
    }
    public static Map<ResourceLocation, List<ShopEntry>> customEntries = new HashMap<>();
    public static List<ShopEntry> getShopEntries(ResourceLocation role) {
        if (customEntries.containsKey(role)) {
            return customEntries.get(role);
        }

        return List.of();
    }
}
