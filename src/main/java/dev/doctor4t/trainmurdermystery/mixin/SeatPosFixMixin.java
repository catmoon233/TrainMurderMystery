package dev.doctor4t.trainmurdermystery.mixin;


import dev.doctor4t.trainmurdermystery.block.entity.SeatEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class SeatPosFixMixin {
    @Redirect(method = "dismountTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setPos(DDD)V"))
    public void stopRiding(ServerPlayer instance, double x, double y, double z) {
        instance.setPos(x, y+1.25, z);
//        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
//        if (player.getVehicle() instanceof SeatEntity) {
//            final var add = player.getPos().add(0, 1, 0);
//            player.requestTeleport(add.x, add.y, add.z);
//        }
    }
}
