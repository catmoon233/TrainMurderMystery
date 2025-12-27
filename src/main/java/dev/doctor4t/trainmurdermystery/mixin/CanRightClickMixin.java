package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.upcraft.datasync.api.ext.DataSyncPlayerExt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class CanRightClickMixin extends LivingEntity implements DataSyncPlayerExt {
    protected CanRightClickMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "canInteractWithBlock",at = @At("TAIL"), cancellable = true)
    public void canInteractWithBlockAt(BlockPos pos, double additionalRange, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue())return;
        if ( GameFunctions.isPlayerAliveAndSurvival((Player) (Object) (this))) {
            final var block = level().getBlockState(pos).getBlock();
            if (!block.equals(Blocks.LECTERN) && !(level().registryAccess().registryOrThrow(BuiltInRegistries.BLOCK.key()).getKey(block).getNamespace().equals(TMM.MOD_ID) && !(level().registryAccess().registryOrThrow(BuiltInRegistries.BLOCK.key()).getKey(block).getNamespace().equals("minopp") ))) {
                cir.setReturnValue(false);
            }
        }
    }
}
