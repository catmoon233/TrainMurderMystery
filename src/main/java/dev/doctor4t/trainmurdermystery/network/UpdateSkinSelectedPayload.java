package dev.doctor4t.trainmurdermystery.network;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import dev.doctor4t.trainmurdermystery.client.StatusBarHUD;
import dev.doctor4t.trainmurdermystery.client.StatusInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateSkinSelectedPayload(String id, String name) implements CustomPacketPayload {
    public static final Type<UpdateSkinSelectedPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(TMM.MOD_ID, "update_skin_selected"));
    public static final StreamCodec<FriendlyByteBuf, UpdateSkinSelectedPayload> CODEC = StreamCodec.ofMember(UpdateSkinSelectedPayload::encode, UpdateSkinSelectedPayload::decode);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(name);

    }

    public static UpdateSkinSelectedPayload decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        String name = buf.readUtf();
        return new UpdateSkinSelectedPayload(id,name);
    }
    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            context.server().execute(() -> {
                PlayerSkinsComponent playerSkinsComponent = PlayerSkinsComponent.KEY.get(context.player());
                if (!playerSkinsComponent.isSkinUnlockedForItemType(payload.id, payload.name))return;
                playerSkinsComponent.setEquippedSkinForItemType(payload.id, payload.name);
                playerSkinsComponent.sync();

            });
        });
    }
    }