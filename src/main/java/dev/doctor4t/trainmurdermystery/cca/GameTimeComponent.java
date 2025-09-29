package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class GameTimeComponent implements AutoSyncedComponent, CommonTickingComponent {
    public static final ComponentKey<GameTimeComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("time"), GameTimeComponent.class);
    public final World world;
    public int time;

    public GameTimeComponent(World world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    public void reset() {
        this.setTime(GameConstants.TIME_AT_GAME_START);
    }

    @Override
    public void tick() {
        if (!TMMComponents.GAME.get(this.world).isRunning()) return;
        if (this.time <= 0) return;
        this.time--;
        if (this.time % 400 == 0) this.sync();
    }

    public boolean hasTime() {
        return this.time > 0;
    }

    public int getTime() {
        return this.time;
    }

    public void addTime(int time) {
        this.setTime(this.time + time);
    }

    public void setTime(int time) {
        this.time = time;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("time", this.time);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.time = tag.getInt("time");
    }
}