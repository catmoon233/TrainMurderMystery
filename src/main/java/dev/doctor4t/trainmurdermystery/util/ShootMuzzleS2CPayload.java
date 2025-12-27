package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.index.TMMParticles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record ShootMuzzleS2CPayload(int shooterId) implements CustomPacketPayload {
    public static final Type<ShootMuzzleS2CPayload> ID = new Type<>(TMM.id("shoot_muzzle_s2c"));
    public static final StreamCodec<FriendlyByteBuf, ShootMuzzleS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ShootMuzzleS2CPayload::shooterId, ShootMuzzleS2CPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<ShootMuzzleS2CPayload> {
        @Override
        public void receive(@NotNull ShootMuzzleS2CPayload payload, ClientPlayNetworking.@NotNull Context context) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                if (client.level == null || client.player == null) return;
                Entity entity = client.level.getEntity(payload.shooterId());
                if (!(entity instanceof Player shooter)) return;

                if (shooter.getId() == client.player.getId() && client.options.getCameraType() == CameraType.FIRST_PERSON)
                    return;
                Vec3 muzzlePos = MatrixParticleManager.getMuzzlePosForPlayer(shooter);
                if (muzzlePos != null)
                    client.level.addParticle(TMMParticles.GUNSHOT, muzzlePos.x, muzzlePos.y, muzzlePos.z, 0, 0, 0);
            });
        }
    }
}