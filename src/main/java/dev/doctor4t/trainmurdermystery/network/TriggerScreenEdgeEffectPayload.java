package dev.doctor4t.trainmurdermystery.network;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.StaminaRenderer;
import dev.doctor4t.trainmurdermystery.data.MapConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public record TriggerScreenEdgeEffectPayload(int color, long durationMs, float intensity) implements CustomPacketPayload {
    public static final Type<TriggerScreenEdgeEffectPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(TMM.MOD_ID, "trigger_screen_edge_effect"));
    public static final StreamCodec<FriendlyByteBuf, TriggerScreenEdgeEffectPayload> CODEC = StreamCodec.ofMember(TriggerScreenEdgeEffectPayload::encode, TriggerScreenEdgeEffectPayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(color);
        buf.writeLong(durationMs);
        buf.writeFloat(intensity);
    }

    public static TriggerScreenEdgeEffectPayload decode(FriendlyByteBuf buf) {
        int color = buf.readInt();
        long durationMs = buf.readLong();
        float intensity = buf.readFloat();
        return new TriggerScreenEdgeEffectPayload(color, durationMs, intensity);
    }
    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            context.client().execute(() -> {
                StaminaRenderer.triggerScreenEdgeEffect(payload.color, payload.durationMs, payload.intensity);
            });
        });
    }
}
