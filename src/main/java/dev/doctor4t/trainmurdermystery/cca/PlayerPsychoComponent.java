package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.network.RemoveStatusBarPayload;
import dev.doctor4t.trainmurdermystery.network.TriggerStatusBarPayload;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PlayerPsychoComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<PlayerPsychoComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("psycho"),
            PlayerPsychoComponent.class);
    private final Player player;
    public int psychoTicks = -1;
    public int armour = 1;

    public PlayerPsychoComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer sp) {
        return true;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void reset() {
        if (this.psychoTicks >= 1)
            this.stopPsycho();
        if (this.player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new RemoveStatusBarPayload("Psycho"));
        }
        this.sync();
        this.psychoTicks = -1;
    }

    @Override
    public void clientTick() {
        if (this.psychoTicks <= 0)
            return;
        this.psychoTicks--;
        if (this.player.getMainHandItem().is(TMMItems.BAT))
            return;
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            for (int i = 0; i < 9; i++) {
                if (!this.player.getInventory().getItem(i).is(TMMItems.BAT))
                    continue;
                this.player.getInventory().selected = i;
                break;
            }
        }
    }

    @Override
    public void serverTick() {
        if (this.psychoTicks <= 0)
            return;
        // if (this.psychoTicks % 20 == 0)
        // this.player.sendMessage(Text.translatable("game.psycho_mode.time",
        // this.psychoTicks / 20).withColor(Colors.RED), true);
        if (--this.psychoTicks == 0) {
            // this.player.sendMessage(Text.translatable("game.psycho_mode.over").withColor(Colors.RED),
            // true);
            this.stopPsycho();
            this.sync();

        } else {
            if (this.psychoTicks % 40 == 0) { // 2s一次
                this.sync();
            }
        }

    }

    public boolean startPsycho() {
        if (ShopEntry.insertStackInFreeSlot(this.player, new ItemStack(TMMItems.BAT))) {
            this.setPsychoTicks(GameConstants.getPsychoTimer());
            this.setArmour(GameConstants.getPsychoModeArmour());
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(this.player.level());
            gameWorldComponent.setPsychosActive(gameWorldComponent.getPsychosActive() + 1);
            if (player instanceof ServerPlayer serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new TriggerStatusBarPayload("Psycho"));
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        reset();
    }

    public void stopPsycho() {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(this.player.level());
        gameWorldComponent.setPsychosActive(gameWorldComponent.getPsychosActive() - 1);
        this.psychoTicks = -1;
        if (this.player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new RemoveStatusBarPayload("Psycho"));
        }
        this.player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.is(TMMItems.BAT), Integer.MAX_VALUE,
                this.player.inventoryMenu.getCraftSlots());
    }

    public int getArmour() {
        return this.armour;
    }

    public void setArmour(int armour) {
        this.armour = armour;
        this.sync();
    }

    public int getPsychoTicks() {
        return this.psychoTicks;
    }

    public void setPsychoTicks(int ticks) {
        this.psychoTicks = ticks;
        this.sync();
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("psychoTicks", this.psychoTicks);
        tag.putInt("armour", this.armour);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.psychoTicks = tag.contains("psychoTicks") ? tag.getInt("psychoTicks") : 0;
        this.armour = tag.contains("armour") ? tag.getInt("armour") : 1;
    }
}