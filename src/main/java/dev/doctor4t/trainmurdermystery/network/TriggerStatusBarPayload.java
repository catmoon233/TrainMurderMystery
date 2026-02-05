package dev.doctor4t.trainmurdermystery.network;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.StaminaRenderer;
import dev.doctor4t.trainmurdermystery.client.StatusBarHUD;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TriggerStatusBarPayload(String id, String name, float currentProgress, float maxProgress, long durationMs) implements CustomPacketPayload {
    public static final Type<TriggerStatusBarPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(TMM.MOD_ID, "trigger_status_bar"));
    public static final StreamCodec<FriendlyByteBuf, TriggerStatusBarPayload> CODEC = StreamCodec.ofMember(TriggerStatusBarPayload::encode, TriggerStatusBarPayload::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(name);
        buf.writeFloat(currentProgress);
        buf.writeFloat(maxProgress);
        buf.writeLong(durationMs);
    }

    public static TriggerStatusBarPayload decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        String name = buf.readUtf();
        float currentProgress = buf.readFloat();
        float maxProgress = buf.readFloat();
        long durationMs = buf.readLong();
        return new TriggerStatusBarPayload(id, name, currentProgress, maxProgress, durationMs);
    }
    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            context.client().execute(() -> {
                StatusBarHUD.getInstance().addStatusBar(payload.id, payload.name, payload.currentProgress, payload.maxProgress, payload.durationMs);

            });
        });
    }
}
