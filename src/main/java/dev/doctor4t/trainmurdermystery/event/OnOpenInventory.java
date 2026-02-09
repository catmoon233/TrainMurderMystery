package dev.doctor4t.trainmurdermystery.event;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

public interface OnOpenInventory {

    /**
     * Callback for determining whether an {@link ItemStack} should drop when player
     * died
     */
    Event<OnOpenInventory> EVENT = createArrayBacked(OnOpenInventory.class,
            listeners -> (sl, screen) -> {
                for (OnOpenInventory listener : listeners) {
                    if (listener.needOpenLimittedInventory(sl, screen)) {
                        return true;
                    }
                }
                return false;
            });

    boolean needOpenLimittedInventory(LocalPlayer localPlayer, Screen screen);
}
