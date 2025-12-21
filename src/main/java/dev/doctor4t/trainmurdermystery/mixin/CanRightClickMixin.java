package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.upcraft.datasync.api.ext.DataSyncPlayerExt;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class CanRightClickMixin extends LivingEntity implements DataSyncPlayerExt {
    protected CanRightClickMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "canInteractWithBlockAt",at = @At("TAIL"), cancellable = true)
    public void canInteractWithBlockAt(BlockPos pos, double additionalRange, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue())return;
        if ( TMMClient.isPlayerAliveAndInSurvival()) {
            final var block = getWorld().getBlockState(pos).getBlock();
            if (!block.equals(Blocks.LECTERN) && !(getWorld().getRegistryManager().get(Registries.BLOCK.getKey()).getId(block).getNamespace().equals(TMM.MOD_ID) )) {
                cir.setReturnValue(false);
            }
        }
    }
}
