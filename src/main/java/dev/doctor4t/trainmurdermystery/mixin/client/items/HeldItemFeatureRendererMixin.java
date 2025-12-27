package dev.doctor4t.trainmurdermystery.mixin.client.items;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandLayer.class)
public class HeldItemFeatureRendererMixin {
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack tmm$hideNoteAndRenderPsychosisItems(LivingEntity instance, Operation<ItemStack> original) {
        ItemStack ret = original.call(instance);

        if (ret.is(TMMItems.NOTE)) {
            ret = ItemStack.EMPTY;
        }

        if (TMMClient.moodComponent != null && TMMClient.moodComponent.isLowerThanMid()) { // make sure it's only the main hand item that's being replaced
            HashMap<UUID, ItemStack> psychosisItems = TMMClient.moodComponent.getPsychosisItems();
            UUID uuid = instance.getUUID();
            if (psychosisItems.containsKey(uuid)) {
                ret = psychosisItems.get(uuid);
            }
        }

        return ret;
    }
}
