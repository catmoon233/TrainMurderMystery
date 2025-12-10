package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.index.TMMParticles;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public record ShootMuzzleS2CPayload(int shooterId) implements CustomPayload {
    public static final Id<ShootMuzzleS2CPayload> ID = new Id<>(TMM.id("shoot_muzzle_s2c"));
    public static final PacketCodec<PacketByteBuf, ShootMuzzleS2CPayload> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, ShootMuzzleS2CPayload::shooterId, ShootMuzzleS2CPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<ShootMuzzleS2CPayload> {
        @Override
        public void receive(@NotNull ShootMuzzleS2CPayload payload, ClientPlayNetworking.@NotNull Context context) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                if (client.world == null || client.player == null) return;
                Entity entity = client.world.getEntityById(payload.shooterId());
                if (!(entity instanceof PlayerEntity shooter)) return;

                if (shooter.getId() == client.player.getId() && client.options.getPerspective() == Perspective.FIRST_PERSON)
                    return;
                Vec3d muzzlePos = MatrixParticleManager.getMuzzlePosForPlayer(shooter);
                if (muzzlePos != null)
                    client.world.addParticle(TMMParticles.GUNSHOT, muzzlePos.x, muzzlePos.y, muzzlePos.z, 0, 0, 0);
            });
        }
    }
}