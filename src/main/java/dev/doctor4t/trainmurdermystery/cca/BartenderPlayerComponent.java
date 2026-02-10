package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class BartenderPlayerComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<BartenderPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(TMM.MOD_ID, "bartender"), BartenderPlayerComponent.class);
    private final Player player;
    public int glowTicks = 0;

    public int getArmor() {
        return armor;
    }

    private int armor = 0;

    public void removeArmor() {
        --this.armor;
        this.sync();
    }

    public void removeArmor(int amount) {
        this.armor -= amount;
        this.sync();
    }

    public void reset() {
        this.glowTicks = 0;
        this.armor = 0;
        this.sync();
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return true;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public BartenderPlayerComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
        if (this.glowTicks > 2) {
            --this.glowTicks;
        }
    }

    public static int tick_ = 0;

    public void serverTick() {
        if (this.glowTicks > 0) {
            --this.glowTicks;
            if (++tick_ % 60 == 0) {
                this.sync();
            }
        }

    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return this.player == player;
    }

    public boolean giveArmor() {
        armor = 1;
        this.sync();
        return true;
    }

    public boolean startGlow() {
        setGlowTicks(GameConstants.getInTicks(0, TMMConfig.bartenderGlowDuration));
        this.sync();
        return true;
    }

    public void setGlowTicks(int ticks) {
        this.glowTicks = ticks;
        this.sync();
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("glowTicks", this.glowTicks);
        tag.putInt("armor", this.armor);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.glowTicks = tag.contains("glowTicks") ? tag.getInt("glowTicks") : 0;
        this.armor = tag.contains("armor") ? tag.getInt("armor") : 0;
    }

    @Override
    public void clear() {
        this.glowTicks = 0;
        this.armor = 0;
        this.sync();
    }
}
