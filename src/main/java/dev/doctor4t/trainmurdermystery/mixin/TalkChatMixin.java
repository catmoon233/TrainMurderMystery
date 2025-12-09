package dev.doctor4t.trainmurdermystery.mixin;

import com.kreezcraft.localizedchat.commands.TalkChat;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TalkChat.class)
public class TalkChatMixin {
    @Inject(method = "isPlayerOpped", at = @At("RETURN"), cancellable = true)
    private static void execute(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() == false){
            if(
                    TMMClient.gameComponent == null || !TMMClient.gameComponent.isRunning() || !TMMClient.isPlayerAliveAndInSurvival()
            ){
                cir.setReturnValue(true);
            }

        }
    }
}
