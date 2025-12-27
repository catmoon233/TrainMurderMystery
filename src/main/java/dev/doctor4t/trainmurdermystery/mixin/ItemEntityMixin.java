package dev.doctor4t.trainmurdermystery.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    public abstract @Nullable Entity getOwner();

    @Shadow
    private @Nullable UUID thrower;

    @Shadow
    public abstract ItemStack getItem();

    @WrapMethod(method = "playerTouch")
    public void tmm$preventGunPickup(Player player, Operation<Void> original) {
        if (player.isCreative() || !this.getItem().is(TMMItemTags.GUNS) || (GameWorldComponent.KEY.get(player.level()).isInnocent(player) && !player.equals(this.getOwner()) && !player.getInventory().contains(itemStack -> itemStack.is(TMMItemTags.GUNS)))) {
            original.call(player);
        }
    }
}
