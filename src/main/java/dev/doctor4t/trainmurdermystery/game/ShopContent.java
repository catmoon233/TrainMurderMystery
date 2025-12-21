package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopContent {
    public static List<ShopEntry> defaultEntries = new ArrayList<>();
    static {
        defaultEntries.add(new ShopEntry(TMMItems.KNIFE.getDefaultStack(), TMMConfig.knifePrice, ShopEntry.Type.WEAPON));
        defaultEntries.add(new ShopEntry(TMMItems.REVOLVER.getDefaultStack(), TMMConfig.revolverPrice, ShopEntry.Type.WEAPON));
        defaultEntries.add(new ShopEntry(TMMItems.GRENADE.getDefaultStack(), TMMConfig.grenadePrice, ShopEntry.Type.WEAPON));
        defaultEntries.add(new ShopEntry(TMMItems.PSYCHO_MODE.getDefaultStack(), TMMConfig.psychoModePrice, ShopEntry.Type.WEAPON) {
            @Override
            public boolean onBuy(@NotNull PlayerEntity player) {
                return PlayerShopComponent.usePsychoMode(player);
            }
        });
        defaultEntries.add(new ShopEntry(TMMItems.POISON_VIAL.getDefaultStack(), TMMConfig.poisonVialPrice, ShopEntry.Type.POISON));
        defaultEntries.add(new ShopEntry(TMMItems.SCORPION.getDefaultStack(), TMMConfig.scorpionPrice, ShopEntry.Type.POISON));
        defaultEntries.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), TMMConfig.firecrackerPrice, ShopEntry.Type.TOOL));
        defaultEntries.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultStack(), TMMConfig.lockpickPrice, ShopEntry.Type.TOOL));
        defaultEntries.add(new ShopEntry(TMMItems.CROWBAR.getDefaultStack(), TMMConfig.crowbarPrice, ShopEntry.Type.TOOL));
        defaultEntries.add(new ShopEntry(TMMItems.BODY_BAG.getDefaultStack(), TMMConfig.bodyBagPrice, ShopEntry.Type.TOOL));
        defaultEntries.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultStack(), TMMConfig.blackoutPrice, ShopEntry.Type.TOOL) {
            @Override
            public boolean onBuy(@NotNull PlayerEntity player) {
                return PlayerShopComponent.useBlackout(player);
            }
        });
        defaultEntries.add(new ShopEntry(new ItemStack(TMMItems.NOTE, 4), TMMConfig.notePrice, ShopEntry.Type.TOOL));
    }
    public static Map<Identifier, List<ShopEntry>> customEntries = new HashMap<>();
    public static List<ShopEntry> getShopEntries(Identifier role) {
        if (customEntries.containsKey(role)) return customEntries.get(role);
        return defaultEntries;
    }
}
