package dev.doctor4t.trainmurdermystery.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightTexture.class)
public abstract class TrueDarknessLightmapTextureManagerMixin {
    @WrapOperation(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;", ordinal = 0))
    private Vector3f tmm$fuckYourBlueAssHueMojang(Vector3f instance, Vector3fc other, float t, Operation<Vector3f> original) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;

        return original.call(instance, other, t);
    }

    @WrapOperation(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;", ordinal = 6))
    private Vector3f tmm$trueDarknessAndSunLight(Vector3f instance, Vector3fc other, float t, Operation<Vector3f> original) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel world = client.level;

        if (client.player != null && world != null) {
            return original.call(instance, new Vector3f(.6f, .6f, .6f), Mth.lerp(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false), TMMClient.prevInstinctLightLevel, TMMClient.instinctLightLevel));
        }

        return original.call(instance, other, t);
    }

    @ModifyVariable(method = "updateLightTexture", at = @At(value = "STORE"), ordinal = 2)
    private float tmm$keepSkylight(float value) {
        return value;
    }
}
