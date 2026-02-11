package dev.doctor4t.trainmurdermystery.mixin;

import com.kreezcraft.localizedchat.commands.TalkChat;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TalkChat.class)
public class TalkChatMixin {
    @Inject(method = "isPlayerOpped", at = @At("RETURN"), cancellable = true)
    private static void execute(MinecraftServer server, ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() == false){
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            if(
                    gameWorldComponent == null || !gameWorldComponent.isRunning() || !player.isSpectator()
            ){
                cir.setReturnValue(true);
            }

        }
    }
}
