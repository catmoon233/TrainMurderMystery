package dev.doctor4t.trainmurdermystery.mixin.client.restrictions;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @WrapMethod(method = "processF3")
    private boolean tmm$disableF3Keybinds(int key, Operation<Boolean> original) {
        if (TMMClient.isPlayerAliveAndInSurvival()) {
            return key == 293 ? original.call(key) : false;
        } else {
            return original.call(key);
        }
    }
}
