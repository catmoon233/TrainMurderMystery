package dev.doctor4t.trainmurdermystery.mixin.client.events;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

public interface AllowItemShowInHand {

    /**
     * Event callback to determine if a player is allowed to die for a specific
     * death type.
     * The game currently has the following death type names defined:
     * 'fell_out_of_train', 'poison', 'grenade', 'bat_hit', 'gun_shot',
     * 'knife_stab'.
     * Any other death type not explicitly defined will default to 'generic'.
     * 
     * @see dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons
     */
    Event<AllowItemShowInHand> EVENT = createArrayBacked(AllowItemShowInHand.class,
            listeners -> (player, itemStack) -> {
                for (AllowItemShowInHand listener : listeners) {
                    var a = listener.allowShowInHand(player, itemStack);
                    if (a != null) {
                        return a;
                    }
                }
                return null;
            });

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    ItemStack allowShowInHand(Player player, ItemStack itemStack);
}