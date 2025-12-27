package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.ShopContent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.List;

public class PlayerShopComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<PlayerShopComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("shop"), PlayerShopComponent.class);
    private final Player player;
    public int balance = 0;

    public PlayerShopComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void reset() {
        this.balance = 0;
        this.sync();
    }

    public void addToBalance(int amount) {
        this.setBalance(this.balance + amount);
    }

    public void setBalance(int amount) {
        if (this.balance != amount) {
            this.balance = amount;
            this.sync();
        }
    }

    public void tryBuy(int index) {
        if (index < 0 || index >= getShopEntries().size()) return;
        ShopEntry entry = getShopEntries().get(index);
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && this.balance < entry.price())
            this.balance = entry.price() * 10;
        if (this.balance >= entry.price() && !this.player.getCooldowns().isOnCooldown(entry.stack().getItem()) && entry.onBuy(this.player)) {
            this.balance -= entry.price();
            if (this.player instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 0.9f + this.player.getRandom().nextFloat() * 0.2f, player.getRandom().nextLong()));
            }
        } else {
            this.player.displayClientMessage(Component.literal("购买失败").withStyle(ChatFormatting.DARK_RED), true);
            if (this.player instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 0.9f + this.player.getRandom().nextFloat() * 0.2f, player.getRandom().nextLong()));
            }
        }
        this.sync();
    }
    public static boolean isPlayerAliveAndSurvival(Player player) {
        return player != null && !player.isSpectator() && !player.isCreative();
    }
    private @NotNull List<ShopEntry> getShopEntries() {

        final var gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        final var role = gameWorldComponent.getRole(player);
        if (gameWorldComponent!=null && role!=null && isPlayerAliveAndSurvival( player)) {
            final var shopEntries = ShopContent.getShopEntries(
                    role.getIdentifier()
            );
            if (!shopEntries.isEmpty()) {
                return shopEntries;
            }
            if (gameWorldComponent.canUseKillerFeatures(player)) {
                return ShopContent.defaultEntries;
            }
        }
        return List.of();
    }

    @Override
    public void clientTick() {

    }

    @Override
    public void serverTick() {

    }

    public static boolean useBlackout(@NotNull Player player) {
        player.getCooldowns().addCooldown(TMMItems.BLACKOUT, GameConstants.ITEM_COOLDOWNS.getOrDefault(TMMItems.BLACKOUT, 0));
        return WorldBlackoutComponent.KEY.get(player.level()).triggerBlackout();
    }

    public static boolean usePsychoMode(@NotNull Player player) {
        player.getCooldowns().addCooldown(TMMItems.PSYCHO_MODE, GameConstants.ITEM_COOLDOWNS.getOrDefault(TMMItems.PSYCHO_MODE, 0));
        return PlayerPsychoComponent.KEY.get(player).startPsycho();
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("Balance", this.balance);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.balance = tag.getInt("Balance");
    }
}