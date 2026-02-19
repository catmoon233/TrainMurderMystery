package dev.doctor4t.trainmurdermystery.mixin.client.restrictions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
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
        final var instance = Minecraft.getInstance();
        if (instance == null)
            return false;
        if (instance.options == null)
            return false;
        if (instance.player == null)
            return false;
        if (TMMClient.isInLobby) {
            return false;
        }
        if (!TMMClient.isPlayerCreative() && this.same(instance.options.keyDrop)) {
            if (TMM.canDropItem
                    .contains(BuiltInRegistries.ITEM.getKey(instance.player.getMainHandItem().getItem()).toString())) {
                if (instance.screen == null) {
                    return false;
                }
            }
            return true;
        }
        if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRunning()
                && TMMClient.isPlayerAliveAndInSurvival()) {
            if (this.same(instance.options.keyJump)) {
                if (TMMClient.gameComponent.isJumpAvailable())
                    return false;
                return true;
            }
            return this.same(instance.options.keySwapOffhand) ||
                    this.same(instance.options.keyTogglePerspective) ||

                    this.same(instance.options.keyAdvancements);
        }
        return false;
    }

    @ModifyReturnValue(method = "consumeClick", at = @At("RETURN"))
    private boolean tmm$restrainWasPressedKeys(boolean original) {
        if (this.shouldSuppressKey())
            return false;
        else
            return original;
    }

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean tmm$restrainIsPressedKeys(boolean original) {
        if (this.shouldSuppressKey())
            return false;
        else
            return original;
    }

    @ModifyReturnValue(method = "matches", at = @At("RETURN"))
    private boolean tmm$restrainMatchesKey(boolean original) {
        if (this.shouldSuppressKey())
            return false;
        else
            return original;
    }
}
