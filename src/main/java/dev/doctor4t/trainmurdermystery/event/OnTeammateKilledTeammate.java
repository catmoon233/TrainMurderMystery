package dev.doctor4t.trainmurdermystery.event;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.level.ServerPlayer;

import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

public interface OnTeammateKilledTeammate {
    Event<OnTeammateKilledTeammate> EVENT = createArrayBacked(OnTeammateKilledTeammate.class,
            listeners -> (victim, killer, isInnocent) -> {
                for (OnTeammateKilledTeammate listener : listeners) {
                    listener.playerKilled(victim, killer, isInnocent);
                }
            });

    void playerKilled(ServerPlayer victim, ServerPlayer killer, boolean isInnocent);
}