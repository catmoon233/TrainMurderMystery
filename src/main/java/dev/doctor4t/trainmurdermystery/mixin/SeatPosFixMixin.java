package dev.doctor4t.trainmurdermystery.mixin;


import dev.doctor4t.trainmurdermystery.block.entity.SeatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class SeatPosFixMixin {
    @Inject(method = "stopRiding", at = @At("HEAD"))
    public void stopRiding(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getVehicle() instanceof SeatEntity) {
            final var add = player.getPos().add(0, 1, 0);
            player.requestTeleport(add.x, add.y, add.z);
        }
    }
}
