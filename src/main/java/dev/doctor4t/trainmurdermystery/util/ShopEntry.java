package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopEntry {
    private final ItemStack stack;
    private final int price;
    private final Type type;

    public enum Type {
        WEAPON("gui/shop_slot_weapon"),
        POISON("gui/shop_slot_poison"),
        TOOL("gui/shop_slot_tool");

        final ResourceLocation texture;

        Type(String texture) {
            this.texture = TMM.id(texture);
        }

        public ResourceLocation getTexture() {
            return texture;
        }
    }

    public ShopEntry(ItemStack stack, int price, Type type) {
        this.stack = stack;
        this.price = price;
        this.type = type;
    }

    public boolean onBuy(@NotNull Player player) {
        if (GameWorldComponent.KEY.get(player.level()).canUseKillerFeatures(player)) {
            return insertStackInFreeSlot(player, this.stack.copy());
        } else return false;
    }

    public static boolean insertStackInFreeSlot(@NotNull Player player, ItemStack stackToInsert) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                player.getInventory().setItem(i, stackToInsert);
                return true;
            }
        }
        return false;
    }

    public ItemStack stack() {
        return this.stack;
    }

    public int price() {
        return this.price;
    }

    public Type type() {
        return type;
    }
}