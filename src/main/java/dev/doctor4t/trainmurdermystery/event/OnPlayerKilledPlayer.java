package dev.doctor4t.trainmurdermystery.event;


import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.level.ServerPlayer;

import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

public interface OnPlayerKilledPlayer {

    Event<OnPlayerKilledPlayer> EVENT = createArrayBacked(OnPlayerKilledPlayer.class, listeners -> (victim, killer) -> {
        for (OnPlayerKilledPlayer listener : listeners) {
            listener.playerKiller(victim, killer);
        }
    });

    void playerKiller(ServerPlayer victim, ServerPlayer killer);
}