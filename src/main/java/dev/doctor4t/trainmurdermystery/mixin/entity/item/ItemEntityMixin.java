package dev.doctor4t.trainmurdermystery.mixin.entity.item;

import dev.doctor4t.trainmurdermystery.TMM;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import dev.doctor4t.trainmurdermystery.util.TMMItemUtils;

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
        if (player.isCreative() || TMM.isLobby) {
            original.call(player);
            return;
        }
        if (!this.getItem().is(TMMItemTags.GUNS)) {
            if (dev.doctor4t.trainmurdermystery.api.RoleMethodDispatcher.callOnPickupItem(player,
                    this.getItem().getItem())) {
                original.call(player);
            }
            return;
        }
        if ((GameWorldComponent.KEY.get(player.level()).canPickUpRevolver(player)
                && !player.equals(this.getOwner()))) {
            // 在拾取物品之前调用角色的onPickupItem方法
            if (TMMItemUtils.hasItem(player, TMMItemTags.GUNS) > 0) {
                return;
            }
            if (dev.doctor4t.trainmurdermystery.api.RoleMethodDispatcher.callOnPickupItem(player,
                    this.getItem().getItem())) {
                original.call(player);
            }
        }
    }
}