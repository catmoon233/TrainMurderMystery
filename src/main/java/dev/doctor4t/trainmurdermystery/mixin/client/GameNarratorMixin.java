package dev.doctor4t.trainmurdermystery.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.text2speech.Narrator;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.network.chat.Component;

@Mixin(GameNarrator.class)
public class GameNarratorMixin {
    @Shadow
    @Final
    private Narrator narrator;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "sayChat", at = @At("HEAD"), cancellable = true)
    private void disablesayChat(CallbackInfo cir) {
        if (!TMMClient.isInLobby) {
            cir.cancel();
        }
    }

    @Inject(method = "getStatus", at = @At("HEAD"), cancellable = true)
    private void disableStatus(CallbackInfoReturnable<NarratorStatus> cir) {
        if (!TMMClient.isInLobby) {
            NarratorStatus status = (NarratorStatus) this.minecraft.options.narrator().get();

            if (!status.equals(NarratorStatus.OFF) && !status.equals(NarratorStatus.SYSTEM)) {
                String string = Component.translatable("warning.narrator").getString();
                if (this.narrator != null)
                    this.narrator.say(string, false);
            }
            cir.setReturnValue(NarratorStatus.OFF);
            cir.cancel();
        }
    }
}
