
package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent;
import dev.doctor4t.trainmurdermystery.item.CocktailItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CocktailItem.class)
public class CocktailItemMixin {

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    public void bartenderVision(ItemStack stack, Level world, LivingEntity user,
            CallbackInfoReturnable<ItemStack> cir) {
        ((BartenderPlayerComponent) BartenderPlayerComponent.KEY.get(user)).startGlow();
    }
}
