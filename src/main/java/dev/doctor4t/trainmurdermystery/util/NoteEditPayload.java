package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerNoteComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.NotNull;

public record NoteEditPayload(String line1, String line2, String line3, String line4) implements CustomPayload {
    public static final Id<NoteEditPayload> ID = new Id<>(TMM.id("note"));
    public static final PacketCodec<PacketByteBuf, NoteEditPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, NoteEditPayload::line1, PacketCodecs.STRING, NoteEditPayload::line2, PacketCodecs.STRING, NoteEditPayload::line3, PacketCodecs.STRING, NoteEditPayload::line4, NoteEditPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<NoteEditPayload> {
        @Override
        public void receive(@NotNull NoteEditPayload payload, ServerPlayNetworking.@NotNull Context context) {
            PlayerNoteComponent.KEY.get(context.player()).setNote(payload.line1(), payload.line2(), payload.line3(), payload.line4());
        }
    }
}