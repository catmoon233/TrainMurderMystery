package dev.doctor4t.trainmurdermystery.mixin.client;

import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.item.ItemWithSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Item.class)
public class ItemSkinMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void arsenal$setTridentOwner(ItemStack itemStack, Level level, Entity entity, int i, boolean bl, CallbackInfo ci) {
        if (itemStack.getItem() instanceof ItemWithSkin && entity instanceof Player player) {
            if (itemStack.get(TMMDataComponentTypes.OWNER) == null) {
                itemStack.set(TMMDataComponentTypes.OWNER, player.getUUID().toString());
            }
        }
    }
}


