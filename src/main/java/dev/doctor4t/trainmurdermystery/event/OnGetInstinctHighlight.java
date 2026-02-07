package dev.doctor4t.trainmurdermystery.event;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import static net.fabricmc.fabric.api.event.EventFactory.createArrayBacked;

public interface OnGetInstinctHighlight {

    /**
     * Callback for determining whether an {@link ItemStack} should drop when player
     * died
     */
    Event<OnGetInstinctHighlight> EVENT = createArrayBacked(OnGetInstinctHighlight.class,
            listeners -> (stack, isInstinctEnabled) -> {
                for (OnGetInstinctHighlight listener : listeners) {
                    int color = listener.GetInstinctHighlight(stack, isInstinctEnabled);
                    if (color != -1) {
                        return color;
                    }
                }
                return -1;
            });

    int GetInstinctHighlight(Entity target, boolean isInstinctEnabled);
}
