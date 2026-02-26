package dev.doctor4t.trainmurdermystery.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.doctor4t.trainmurdermystery.client.TMMClient;

import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

@Mixin(ClientPacketListener.class)
public class SkinGetterMixin {
    @Inject(method = "handlePlayerInfoUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/PlayerInfo;<init>(Lcom/mojang/authlib/GameProfile;Z)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void afterPlayerInfoCreate(ClientboundPlayerInfoUpdatePacket packet,
            CallbackInfo ci,
            ClientboundPlayerInfoUpdatePacket.Entry entry,
            PlayerInfo playerInfo) {
        TMMClient.PLAYER_ENTRIES_CACHE.putIfAbsent(playerInfo.getProfile().getId(), playerInfo);
        // 在这里你就可以使用 entry 和 playerInfo 了
        System.out.println("新玩家信息：" + playerInfo.getProfile().getName());
        // 你可以添加自己的逻辑，例如记录日志、修改数据等
    }
}
