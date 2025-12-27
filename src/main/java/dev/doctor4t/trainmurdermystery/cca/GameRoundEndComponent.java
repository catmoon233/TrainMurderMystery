package dev.doctor4t.trainmurdermystery.cca;

import com.mojang.authlib.GameProfile;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class GameRoundEndComponent implements AutoSyncedComponent {
    public static final ComponentKey<GameRoundEndComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("roundend"), GameRoundEndComponent.class);
    private final Level world;
    private final List<RoundEndData> players = new ArrayList<>();
    private GameFunctions.WinStatus winStatus = GameFunctions.WinStatus.NONE;

    public GameRoundEndComponent(Level world) {
        this.world = world;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    public void setRoundEndData(@NotNull List<ServerPlayer> players, GameFunctions.WinStatus winStatus) {
        this.players.clear();
        for (ServerPlayer player : players) {
            RoleAnnouncementTexts.RoleAnnouncementText role = RoleAnnouncementTexts.BLANK;
            GameWorldComponent game = GameWorldComponent.KEY.get(this.world);
            if (game.canUseKillerFeatures(player)) {
                role = RoleAnnouncementTexts.KILLER;
            } else if (game.isRole(player, TMMRoles.VIGILANTE)) {
                role = RoleAnnouncementTexts.VIGILANTE;
            } else {
                role = RoleAnnouncementTexts.CIVILIAN;
            }
            this.players.add(new RoundEndData(player.getGameProfile(), role, !GameFunctions.isPlayerAliveAndSurvival(player)));
        }
        this.winStatus = winStatus;
        this.sync();
    }

    public boolean didWin(UUID uuid) {
        if (GameFunctions.WinStatus.NONE == this.winStatus) return false;
        for (RoundEndData detail : this.players) {
            if (!detail.player.getId().equals(uuid)) continue;
            return switch (this.winStatus) {
                case KILLERS -> detail.role == RoleAnnouncementTexts.KILLER;
                case PASSENGERS, TIME -> detail.role != RoleAnnouncementTexts.KILLER;
                default -> false;
            };
        }
        return false;
    }

    public List<RoundEndData> getPlayers() {
        return this.players;
    }

    public GameFunctions.WinStatus getWinStatus() {
        return this.winStatus;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        ListTag list = new ListTag();
        for (RoundEndData detail : this.players) list.add(detail.writeToNbt());
        tag.put("players", list);
        tag.putInt("winstatus", this.winStatus.ordinal());
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.players.clear();
        for (Tag element : tag.getList("players", 10)) this.players.add(new RoundEndData((CompoundTag) element));
        this.winStatus = GameFunctions.WinStatus.values()[tag.getInt("winstatus")];
    }

    public record RoundEndData(GameProfile player, RoleAnnouncementTexts.RoleAnnouncementText role, boolean wasDead) {
        public RoundEndData(@NotNull CompoundTag tag) {
            this(new GameProfile(tag.getUUID("uuid"), tag.getString("name")), RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.get(tag.getInt("role")), tag.getBoolean("wasDead"));
        }

        public @NotNull CompoundTag writeToNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("uuid", this.player.getId());
            tag.putString("name", this.player.getName());
            tag.putInt("role", RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(this.role));
            tag.putBoolean("wasDead", this.wasDead);
            return tag;
        }
    }
}