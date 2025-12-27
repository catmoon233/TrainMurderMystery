package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.client.gui.RoundTextRenderer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record AnnounceWelcomePayload(int role, int killers, int targets) implements CustomPacketPayload {
    public static final Type<AnnounceWelcomePayload> ID = new Type<>(TMM.id("announcewelcome"));
    public static final StreamCodec<FriendlyByteBuf, AnnounceWelcomePayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, AnnounceWelcomePayload::role, ByteBufCodecs.INT, AnnounceWelcomePayload::killers, ByteBufCodecs.INT, AnnounceWelcomePayload::targets, AnnounceWelcomePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ClientPlayNetworking.PlayPayloadHandler<AnnounceWelcomePayload> {
        @Override
        public void receive(@NotNull AnnounceWelcomePayload payload, ClientPlayNetworking.@NotNull Context context) {
            if (payload.role() < 0 || payload.role() >= RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.size()) return;
            RoundTextRenderer.startWelcome(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.get(payload.role()), payload.killers(), payload.targets());
        }
    }
}