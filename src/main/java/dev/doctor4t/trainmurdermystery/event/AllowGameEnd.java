package dev.doctor4t.trainmurdermystery.event;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.level.ServerLevel;
import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;

public interface AllowGameEnd {

    /**
     * Event callback to determine if a game is allowed to stop for a specific
     * win status.
     * The game currently has the following death type names defined:
     * NONE, KILLERS, PASSENGERS, TIME, LOOSE_END, GAMBLER, RECORDER
     * 
     * @see dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons
     */
    Event<AllowGameEnd> EVENT = createArrayBacked(AllowGameEnd.class,
            listeners -> (serverWorld, winStatus, isLooseEndsMode) -> {
                for (AllowGameEnd listener : listeners) {
                    if (!listener.allowGameEnd(serverWorld, winStatus, isLooseEndsMode)) {
                        return false;
                    }
                }
                return true;
            });

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean allowGameEnd(ServerLevel serverWorld, GameFunctions.WinStatus winStatus, boolean isLooseEndsMode);
}