package dev.doctor4t.trainmurdermystery.mixin.client;

import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.resources.SkinManager;
// CacheKey是私有的，不能直接导入
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(SkinManager.class)
public class SkinManageFixer {


    @Shadow
    @Final
    private MinecraftSessionService sessionService;

    @Shadow
    @Final
    private LoadingCache<SkinManager.CacheKey, CompletableFuture<PlayerSkin>> skinCache;


    @Inject(method = "getOrLoad", at = @At("HEAD"), cancellable = true)
    private void fixGetOrLoadConcurrency(GameProfile gameProfile, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir) {
        Property property = this.sessionService.getPackedTextures(gameProfile);
        SkinManager.CacheKey key = new SkinManager.CacheKey(gameProfile.getId(), property);
        try {
            cir.setReturnValue(this.skinCache.getUnchecked(key));

        } catch (Exception ignored){
        }
        cir.cancel();
    }

}