package dev.doctor4t.trainmurdermystery.network.tmm;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AnnounceWelcomePayload(String role, int killers, int targets) implements CustomPacketPayload {
    public static final Type<AnnounceWelcomePayload> ID = new Type<>(TMM.id("announcewelcome"));
    public static final StreamCodec<FriendlyByteBuf, AnnounceWelcomePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AnnounceWelcomePayload::role, ByteBufCodecs.INT, AnnounceWelcomePayload::killers,
            ByteBufCodecs.INT, AnnounceWelcomePayload::targets, AnnounceWelcomePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}