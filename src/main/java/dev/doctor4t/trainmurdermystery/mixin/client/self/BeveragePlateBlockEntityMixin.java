package dev.doctor4t.trainmurdermystery.mixin.client.self;

import dev.doctor4t.trainmurdermystery.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.event.CanSeePoison;
import dev.doctor4t.trainmurdermystery.index.TMMParticles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author SkyNotTheLimit
 */
@Environment(EnvType.CLIENT)
@Mixin(BeveragePlateBlockEntity.class)
public class BeveragePlateBlockEntityMixin {

    // haha I love writing extremely cursed mixins
    @Inject(method = "clientTick", at = @At("HEAD"))
    private static void tickWithoutFearOfCrashing(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof BeveragePlateBlockEntity tray)) {
            return;
        }
        if ((!TMMClient.isKiller() && !CanSeePoison.EVENT.invoker().visible(Minecraft.getInstance().player)) || tray.getPoisoner() == null) {
            return;
        }
        if (world.getRandom().nextIntBetweenInclusive(0, 20) < 17) {
            return;
        }
        world.addParticle(
                TMMParticles.POISON,
                pos.getX() + 0.5f,
                pos.getY(),
                pos.getZ() + 0.5f,
                0f, 0.05f, 0f
        );
    }
}
