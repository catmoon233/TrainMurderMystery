package dev.doctor4t.trainmurdermystery.mixin.client.restrictions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyMapping.class)
public abstract class KeyBindingMixin {
    @Shadow
    public abstract boolean same(KeyMapping other);

    @Unique
    private boolean shouldSuppressKey() {
        if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRunning() && TMMClient.isPlayerAliveAndInSurvival()) {
            return this.same(Minecraft.getInstance().options.keySwapOffhand) ||
                    this.same(Minecraft.getInstance().options.keyJump) ||
                    this.same(Minecraft.getInstance().options.keyTogglePerspective) ||
                    this.same(Minecraft.getInstance().options.keyDrop) ||
                    this.same(Minecraft.getInstance().options.keyAdvancements);
        }
        return false;
    }

    @ModifyReturnValue(method = "consumeClick", at = @At("RETURN"))
    private boolean tmm$restrainWasPressedKeys(boolean original) {
        if (this.shouldSuppressKey()) return false;
        else return original;
    }

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean tmm$restrainIsPressedKeys(boolean original) {
        if (this.shouldSuppressKey()) return false;
        else return original;
    }

    @ModifyReturnValue(method = "matches", at = @At("RETURN"))
    private boolean tmm$restrainMatchesKey(boolean original) {
        if (this.shouldSuppressKey()) return false;
        else return original;
    }
}
