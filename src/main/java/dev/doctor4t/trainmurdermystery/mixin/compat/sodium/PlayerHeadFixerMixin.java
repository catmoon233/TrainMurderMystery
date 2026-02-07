package dev.doctor4t.trainmurdermystery.mixin.compat.sodium;

import com.mojang.authlib.GameProfile;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkullBlockRenderer.class)
public class PlayerHeadFixerMixin {
    @Redirect(method = "getRenderType",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/SkinManager;getInsecureSkin(Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/resources/PlayerSkin;"))
    private static PlayerSkin getRenderType(SkinManager instance, GameProfile gameProfile){
        return TMMClient.PLAYER_ENTRIES_CACHE.get(gameProfile.getId()).getSkin();
    }
}
