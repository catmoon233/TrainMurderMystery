package dev.doctor4t.trainmurdermystery.network.tmm;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AnnounceEndingPayload() implements CustomPacketPayload {
    public static final Type<AnnounceEndingPayload> ID = new Type<>(TMM.id("announceending"));
    public static final StreamCodec<FriendlyByteBuf, AnnounceEndingPayload> CODEC = StreamCodec.unit(new AnnounceEndingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}