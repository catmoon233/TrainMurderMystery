package dev.doctor4t.trainmurdermystery.mixin;

import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncWorldDataMessage;
import net.minecraft.network.RegistryByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSavedDataType.class)
public abstract class MoonlightMessageMixin<D extends WorldSavedData>

{
    @Inject(method = "isSyncable", at = @At("HEAD"), cancellable = true)
    public void isSyncable(CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(false);

    }
}
