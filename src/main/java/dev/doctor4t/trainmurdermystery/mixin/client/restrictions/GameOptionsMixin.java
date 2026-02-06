package dev.doctor4t.trainmurdermystery.mixin.client.restrictions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public class GameOptionsMixin {
    @ModifyReturnValue(method = "getCameraType", at = @At("RETURN"))
    public CameraType getPerspective(CameraType original) {
        if (TMMClient.isInLobby) {
            return original;
        }
        if (GameFunctions.isPlayerAliveAndSurvival(Minecraft.getInstance().player)) {
            if (TMMClient.gameComponent!=null) {
                final var role = TMMClient.gameComponent.getRole(Minecraft.getInstance().player);
                if (role != null && TMM.canUseOtherPerson.stream().anyMatch(predicate -> predicate.test(role))) {
                    return original;
                }
            }
            return CameraType.FIRST_PERSON;
        } else {
            return original;
        }
    }
}
