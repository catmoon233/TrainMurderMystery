package dev.doctor4t.trainmurdermystery.client.util;

import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class TMMItemTooltips {
    private static final int COOLDOWN_COLOR = 0xC90000;
    private static final int LETTER_COLOR = 0xC5AE8B;
    private static final int REGULAR_TOOLTIP_COLOR = 0x808080;

    public static void addTooltips() {
        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType, tooltipList) -> {
            addTooltipsForItem(TMMItems.KNIFE, 3, itemStack, tooltipList);
            addCooldownText(TMMItems.KNIFE, tooltipList, itemStack);

            addTooltipsForItem(TMMItems.LOCKPICK, 2, itemStack, tooltipList);
            addCooldownText(TMMItems.LOCKPICK, tooltipList, itemStack);

            addRevolverTooltips(itemStack, tooltipList);

            addTooltipsForItem(TMMItems.BODY_BAG, 1, itemStack, tooltipList);

            addTooltipsForItem(TMMItems.BLACKOUT, 3, itemStack, tooltipList);
            addTooltipsForItem(TMMItems.PSYCHO_MODE, 3, itemStack, tooltipList);
        });
    }

    private static void addRevolverTooltips(ItemStack itemStack, List<Text> tooltipList) {
        Item item = TMMItems.REVOLVER;
        if (itemStack.isOf(item)) {
            Integer i = itemStack.get(TMMDataComponentTypes.BULLETS);
            i = i != null ? i : 6;
            tooltipList.add(Text.translatable("tip." + item.getTranslationKey().substring(24) + ".rounds", i).withColor(i <= 0 ? COOLDOWN_COLOR : 0xC1CCD1));
            addTooltipsForItem(item, 2, itemStack, tooltipList);
        }
    }

    private static void addTooltipsForItem(Item item, int tooltipLineCount, ItemStack itemStack, List<Text> tooltipList) {
        if (itemStack.isOf(item)) {
            for (int i = 1; i <= tooltipLineCount; i++) {
                tooltipList.add(Text.translatable("tip." + item.getTranslationKey().substring(24) + ".tooltip" + i).withColor(REGULAR_TOOLTIP_COLOR));
            }
        }
    }

    private static void addCooldownText(Item item, List<Text> tooltipList, ItemStack itemStack) {
        if (itemStack.isOf(item)) {
            ItemCooldownManager itemCooldownManager = MinecraftClient.getInstance().player.getItemCooldownManager();
            if (itemCooldownManager.isCoolingDown(item)) {
                ItemCooldownManager.Entry knifeEntry = itemCooldownManager.entries.get(item);
                int timeLeft = knifeEntry.endTick - itemCooldownManager.tick;

                if (timeLeft > 0) {
                    int minutes = (int) Math.floor((double) timeLeft / 1200);
                    int seconds = (timeLeft - (minutes * 1200)) / 20;
                    String countdown = (minutes > 0 ? minutes + "m" : "") + (seconds > 0 ? seconds + "s" : "");
                    tooltipList.add(Text.translatable("tip.cooldown", countdown).withColor(COOLDOWN_COLOR));
                }
            }
        }
    }
}
