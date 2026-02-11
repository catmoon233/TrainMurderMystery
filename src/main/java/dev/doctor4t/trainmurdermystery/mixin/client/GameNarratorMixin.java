package dev.doctor4t.trainmurdermystery.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.NarratorStatus;
import net.minecraft.network.chat.Component;

@Mixin(GameNarrator.class)
public class GameNarratorMixin {
    @Inject(method = "sayChat", at = @At("HEAD"), cancellable = true)
    private void disableSayChat(Component component, CallbackInfoReturnable<Void> cir) {
        if(!TMM.isLobby){
            cir.cancel();
        }
    }

    @Inject(method = "say", at = @At("HEAD"), cancellable = true)
    private void disableSay(Component component, CallbackInfoReturnable<Void> cir) {
        if(!TMM.isLobby){
            cir.cancel();
        }
    }

    @Inject(method = "getStatus", at = @At("HEAD"), cancellable = true)
    private void disableStatus(CallbackInfoReturnable<NarratorStatus> cir) {
        if(!TMM.isLobby){
            cir.setReturnValue(NarratorStatus.OFF);
        }
    }
}
