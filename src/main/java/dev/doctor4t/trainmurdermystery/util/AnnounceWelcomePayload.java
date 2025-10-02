package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementText;
import dev.doctor4t.trainmurdermystery.client.gui.RoundTextRenderer;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

public record AnnounceWelcomePayload(int role, int killers, int targets) implements CustomPayload {
    public static final Id<AnnounceWelcomePayload> ID = new Id<>(TMM.id("announcewelcome"));
    public static final PacketCodec<PacketByteBuf, AnnounceWelcomePayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, AnnounceWelcomePayload::role, PacketCodecs.INTEGER, AnnounceWelcomePayload::killers, PacketCodecs.INTEGER, AnnounceWelcomePayload::targets, AnnounceWelcomePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<AnnounceWelcomePayload> {
        @Override
        public void receive(@NotNull AnnounceWelcomePayload payload, ClientPlayNetworking.@NotNull Context context) {
            if (payload.role() < 0 || payload.role() >= RoleAnnouncementText.values().length) return;
            RoundTextRenderer.startWelcome(RoleAnnouncementText.values()[payload.role()], payload.killers(), payload.targets());
        }
    }
}