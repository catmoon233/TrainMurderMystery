package dev.doctor4t.trainmurdermystery.network;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.data.MapConfig;
import dev.doctor4t.trainmurdermystery.data.ServerMapConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShowSelectedMapUIPayload(String serverConfig) implements CustomPacketPayload {
    public static final Type<ShowSelectedMapUIPayload> ID = new Type<>(TMM.id("show_selected_map_ui"));
    public static final StreamCodec<FriendlyByteBuf, ShowSelectedMapUIPayload> CODEC = CustomPacketPayload
            .codec(ShowSelectedMapUIPayload::write, ShowSelectedMapUIPayload::new);

    public ShowSelectedMapUIPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf());
    }

    public ShowSelectedMapUIPayload(ServerMapConfig mp) {
        this(convertServerMapConfigToString(mp));
    }

    public static String convertServerMapConfigToString(ServerMapConfig mp) {
        MapConfig cfg = new MapConfig();
        cfg.maps = mp.getRandomMaps();
        return MapConfig.gson.toJson(cfg);
    }

    public ShowSelectedMapUIPayload(MapConfig mp) {
        this(MapConfig.gson.toJson(mp));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(serverConfig);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
