package dev.doctor4t.trainmurdermystery.mixin.client.restrictions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DebugScreenOverlay.class)
public class DebugHudMixin {
    @ModifyReturnValue(method = "showDebugScreen", at = @At("RETURN"))
    public boolean shouldShowDebugHud(boolean original) {
        return !TMMClient.isPlayerAliveAndInSurvival() && original;
    }
}
