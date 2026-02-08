package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.block.MountableBlock;
import dev.doctor4t.trainmurdermystery.block.entity.SeatEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class SeatPosFixMixin {
    @Inject(method = "dismountTo", at = @At("HEAD"), cancellable = true)
    public void stopRiding(double d, double e, double f, CallbackInfo ci) {

        ServerPlayer player = (ServerPlayer) (Object) this;
        var lastPos = MountableBlock.lastPos.get(player.getUUID());
        if (lastPos != null) {
            if (lastPos.distanceTo(player.position()) < 5) {
                player.teleportTo(lastPos.x, lastPos.y + 0.25, lastPos.z);
                MountableBlock.lastPos.remove(player.getUUID());
            }
        }
        ci.cancel();
    }

}
