package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.gui.RoundTextRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record AnnounceEndingPayload() implements CustomPacketPayload {
    public static final Type<AnnounceEndingPayload> ID = new Type<>(TMM.id("announceending"));
    public static final StreamCodec<FriendlyByteBuf, AnnounceEndingPayload> CODEC = StreamCodec.unit(new AnnounceEndingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<AnnounceEndingPayload> {
        @Override
        public void receive(@NotNull AnnounceEndingPayload payload, ClientPlayNetworking.@NotNull Context context) {
            RoundTextRenderer.startEnd();
        }
    }
}