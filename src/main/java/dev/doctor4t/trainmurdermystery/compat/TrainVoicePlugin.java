package dev.doctor4t.trainmurdermystery.compat;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TrainVoicePlugin implements VoicechatPlugin {
    public static final UUID GROUP_ID = UUID.randomUUID();
    public static VoicechatServerApi SERVER_API;
    public static Group GROUP;

    public static boolean isVoiceChatMissing() {
        return SERVER_API == null;
    }

    public static void addPlayer(@NotNull UUID player) {
        if (isVoiceChatMissing())
            return;
        VoicechatConnection connection = SERVER_API.getConnectionOf(player);
        if (connection != null) {
            if (GROUP == null)
                GROUP = SERVER_API.groupBuilder().setHidden(true).setId(GROUP_ID).setName("Train Spectators")
                        .setPersistent(true).setType(Group.Type.OPEN).build();
            if (GROUP != null)
                connection.setGroup(GROUP);
        }
    }

    public static void resetPlayer(@NotNull UUID player) {
        if (isVoiceChatMissing())
            return;
        VoicechatConnection connection = SERVER_API.getConnectionOf(player);
        if (connection != null)
            connection.setGroup(null);
    }

    @Override
    public void registerEvents(@NotNull EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> {
            SERVER_API = event.getVoicechat();
        });

        // registration.registerEvent(PlayerConnectedEvent.class, event -> {
        //     var con = event.getConnection();
        //     var serverPlayer = con.getPlayer();
        //     Object vcServerLevel = serverPlayer.getServerLevel().getServerLevel();
        //     Object vcPlayer = serverPlayer.getPlayer();
        //     if (vcServerLevel instanceof ServerLevel serverLevel) {
        //         final var gameWorldComponent = GameWorldComponent.KEY.get(serverLevel);
        //         if (gameWorldComponent.isRunning()) {
        //             if (vcPlayer instanceof Player player)
        //                 // serverLevel
        //                 if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
        //                     TrainVoicePlugin.addPlayer(player.getUUID());
        //                 }
        //         }
        //     }

        // });
    }

    @Override
    public String getPluginId() {
        return TMM.MOD_ID;
    }
}