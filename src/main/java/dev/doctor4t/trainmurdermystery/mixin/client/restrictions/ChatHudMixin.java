package dev.doctor4t.trainmurdermystery.mixin.client.restrictions;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @WrapMethod(method = "render")
    public void tmm$disableChatRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, Operation<Void> original) {
        final var instance = MinecraftClient.getInstance();
        if (TMMClient.gameComponent!=null && TMMClient.isPlayerAliveAndInSurvival() && instance!=null &&TMMClient.gameComponent.getRole(instance.player)!=null&& TMMClient.gameComponent.getRole(instance.player).identifier().getPath().equals(
            "the_insane_damned_paranoid_killer"
        ) ) {
            return;
        }
        if (TMMClient.gameComponent == null || !TMMClient.gameComponent.isRunning() || !TMMClient.isPlayerAliveAndInSurvival()) {
            original.call(context, currentTick, mouseX, mouseY, focused);
        }
    }
}