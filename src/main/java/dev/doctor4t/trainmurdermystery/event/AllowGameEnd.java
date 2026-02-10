package dev.doctor4t.trainmurdermystery.event;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.server.level.ServerLevel;
import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameFunctions.WinStatus;

public interface AllowGameEnd {

    /**
     * Event callback to determine if a game is allowed to stop for a specific
     * win status.
     * The game currently has the following death type names defined:
     * NONE - DO NOT END,
     * NOT_MODIFY - DO NOT MODIFY (DEFAULT),
     * KILLERS,
     * PASSENGERS,
     * TIME,
     * LOOSE_END,
     * GAMBLER,
     * RECORDER,
     * CUSTOM - 记得修改 GameFunctions.CustomWinnerID 和
     * GameFunctions.CustomWinnersPredicates（判断是否为获胜者）
     * 
     * @see dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons
     */
    Event<AllowGameEnd> EVENT = createArrayBacked(AllowGameEnd.class,
            listeners -> (serverWorld, winStatus, isLooseEndsMode) -> {
                for (AllowGameEnd listener : listeners) {
                    var a = listener.allowGameEnd(serverWorld, winStatus, isLooseEndsMode);
                    if (a != null)
                        if (!a.equals(WinStatus.NOT_MODIFY)) {
                            return a;
                        }
                }
                return WinStatus.NOT_MODIFY;
            });

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    WinStatus allowGameEnd(ServerLevel serverWorld, GameFunctions.WinStatus winStatus, boolean isLooseEndsMode);
}