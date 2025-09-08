package dev.doctor4t.trainmurdermystery.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.trainmurdermystery.client.TrainMurderMysteryClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

    @Shadow
    public abstract boolean equals(KeyBinding other);

    @ModifyReturnValue(method = "wasPressed", at = @At("RETURN"))
    private boolean tmm$restrainKeys(boolean original) {
        if (TrainMurderMysteryClient.shouldRestrictPlayerOptions()) {
            if (this.equals(MinecraftClient.getInstance().options.swapHandsKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.inventoryKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.dropKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.chatKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.commandKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.jumpKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.playerListKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.togglePerspectiveKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.sprintKey)) return false;
            if (this.equals(MinecraftClient.getInstance().options.advancementsKey)) return false;
        }

        return original;
    }
}
