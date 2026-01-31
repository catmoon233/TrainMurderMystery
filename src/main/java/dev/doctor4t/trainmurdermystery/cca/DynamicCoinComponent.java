package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class DynamicCoinComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public void clientTick() {

    }

    @Override
    public void serverTick() {

    }

    @Override
    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {

    }

    @Override
    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {

    }
}
