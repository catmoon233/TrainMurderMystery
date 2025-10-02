package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementText;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.TaskCompletePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEndInfoComponent implements AutoSyncedComponent {
    public static final ComponentKey<PlayerEndInfoComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("endinfo"), PlayerEndInfoComponent.class);
    private final PlayerEntity player;
    public RoleAnnouncementText role = RoleAnnouncementText.CIVILIAN;
    public boolean wasDead = false;

    public PlayerEndInfoComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void renew() {
        var game = TMMComponents.GAME.get(this.player.getWorld());
        if (game.isKiller(this.player)) {
            this.role = RoleAnnouncementText.KILLER;
        } else if (game.isVigilante(this.player)) {
            this.role = RoleAnnouncementText.VIGILANTE;
        } else {
            this.role = RoleAnnouncementText.CIVILIAN;
        }
        this.wasDead = !GameFunctions.isPlayerAliveAndSurvival(this.player);
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("role", this.role.ordinal());
        tag.putBoolean("wasDead", this.wasDead);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("role", NbtElement.INT_TYPE)) {
            var roleIndex = tag.getInt("role");
            if (roleIndex < 0 || roleIndex >= RoleAnnouncementText.values().length) roleIndex = 0;
            this.role = RoleAnnouncementText.values()[roleIndex];
        }
        if (tag.contains("wasDead", NbtElement.BYTE_TYPE)) this.wasDead = tag.getBoolean("wasDead");
    }
}