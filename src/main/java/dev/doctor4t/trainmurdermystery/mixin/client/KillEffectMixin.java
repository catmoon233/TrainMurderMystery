package dev.doctor4t.trainmurdermystery.mixin.client;

import dev.doctor4t.trainmurdermystery.client.StaminaRenderer;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GameFunctions.class)
public class KillEffectMixin {
    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private static void killPlayer(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
        if (killer!=null) {
            if (killer.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
                StaminaRenderer.triggerScreenEdgeEffect(Color.WHITE.getRGB(), 750, 1f);
            }
        }
    }
}
