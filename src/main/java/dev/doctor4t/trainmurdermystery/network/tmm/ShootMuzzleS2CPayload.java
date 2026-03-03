package dev.doctor4t.trainmurdermystery.network.tmm;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShootMuzzleS2CPayload(int shooterId) implements CustomPacketPayload {
    public static final Type<ShootMuzzleS2CPayload> ID = new Type<>(TMM.id("shoot_muzzle_s2c"));
    public static final StreamCodec<FriendlyByteBuf, ShootMuzzleS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ShootMuzzleS2CPayload::shooterId, ShootMuzzleS2CPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}