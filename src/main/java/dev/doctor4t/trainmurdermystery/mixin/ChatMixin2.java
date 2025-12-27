package dev.doctor4t.trainmurdermystery.mixin;

import com.kreezcraft.localizedchat.CommonClass;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommonClass.class)
public class ChatMixin2 {
    @Inject(method = "doPrefix", at = @At("RETURN"), cancellable = true)
    private static void execute(Player mainPlayer, Player comparePlayer, CallbackInfoReturnable<String> cir) {
            if (comparePlayer.isSpectator()){
                cir.setReturnValue("§7[旁观]§r ");

        }
    }
}
