package dev.doctor4t.trainmurdermystery.cca;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMGameModes;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.event.OnTrainAreaHaveReseted;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
// 导入Mth类
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class GameWorldComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<GameWorldComponent> KEY = ComponentRegistry.getOrCreate(TMM.id("game"),
            GameWorldComponent.class);
    private final Level world;
    private boolean canJump = false;
    private boolean lockedToSupporters = false;
    private boolean enableWeights = false;

    public boolean isJumpAvailable() {
        return canJump;
    }

    public void setJumpAvailable(boolean available) {
        this.canJump = available;
        this.sync();
    }

    public boolean isSyncRole() {
        return syncRole;
    }

    public GameWorldComponent setSyncRole(boolean syncRole) {
        this.syncRole = syncRole;
        return this;
    }

    private boolean syncRole = false;

    public void setWeightsEnabled(boolean enabled) {
        this.enableWeights = enabled;
    }

    public boolean areWeightsEnabled() {
        return enableWeights;
    }

    public enum GameStatus {
        INACTIVE, STARTING, ACTIVE, STOPPING
    }

    private GameMode gameMode = TMMGameModes.MURDER;

    private boolean bound = true;

    private GameStatus gameStatus = GameStatus.INACTIVE;
    public int fade = 0;

    private final HashMap<UUID, Role> roles = new HashMap<>();

    public int ticksUntilNextResetAttempt = -1;

    private int psychosActive = 0;

    private UUID looseEndWinner;

    private GameFunctions.WinStatus lastWinStatus = GameFunctions.WinStatus.NONE;

    private float backfireChance = 0f;

    public GameWorldComponent(Level world) {
        this.world = world;
    }

    public void sync() {
        GameWorldComponent.KEY.sync(this.world);
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
        this.sync();
    }

    public int getFade() {
        return fade;
    }

    public void setFade(int fade) {
        this.fade = Mth.clamp(fade, 0, GameConstants.FADE_TIME + GameConstants.FADE_PAUSE);
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
        this.sync();
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public boolean canPickUpRevolver(@NotNull Player player) {
        return getRole(player) != null && getRole(player).canPickUpRevolver();
    }

    public boolean isRunning() {
        return this.gameStatus == GameStatus.ACTIVE || this.gameStatus == GameStatus.STOPPING;
    }

    public void addRole(Player player, Role role) {
        if (player == null) {
            return;
        }
        this.addRole(player.getUUID(), role);
        this.setSyncRole(true);
        this.sync();
        this.setSyncRole(false);
    }

    public void addRole(UUID player, Role role) {
        if (player == null) {
            return;
        }
        this.roles.put(player, role);
        this.setSyncRole(true);
        this.sync();
        this.setSyncRole(false);
    }

    public void resetRole(Role role) {
        roles.entrySet().removeIf(entry -> entry.getValue() == role);
    }

    public void setRoles(List<UUID> players, Role role) {
        if (players == null) {
            return;
        }
        this.setSyncRole(true);
        resetRole(role);

        for (UUID player : players) {
            if (player == null)
                continue;
            addRole(player, role);
        }
        this.sync();
        this.setSyncRole(false);
    }

    public HashMap<UUID, Role> getRoles() {
        return roles;
    }

    public Role getRole(Player player) {
        if (player == null) {
            return null;
        }
        return getRole(player.getUUID());
    }

    public @Nullable Role getRole(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return roles.get(uuid);
    }

    public List<UUID> getAllKillerTeamPlayers() {
        List<UUID> ret = new ArrayList<>();
        roles.forEach((uuid, playerRole) -> {
            if (playerRole.canUseKiller()) {
                ret.add(uuid);
            }
        });

        return ret;
    }

    public List<UUID> getAllWithRole(Role role) {
        List<UUID> ret = new ArrayList<>();
        roles.forEach((uuid, playerRole) -> {
            if (playerRole == role) {
                ret.add(uuid);
            }
        });

        return ret;
    }

    public boolean isRole(@NotNull Player player, Role role) {
        if (player == null) {
            return role == null;
        }
        return isRole(player.getUUID(), role);
    }

    public boolean isRole(@NotNull UUID uuid, Role role) {
        if (uuid == null) {
            return role == null;
        }
        return this.roles.get(uuid) == role;
    }

    public boolean isNeutralForKiller(@NotNull Player player) {
        return getRole(player) != null && getRole(player).isNeutralForKiller();
    }

    public boolean canUseKillerFeatures(@NotNull Player player) {
        return getRole(player) != null && getRole(player).canUseKiller();
    }

    public boolean isInnocent(@NotNull Player player) {
        return getRole(player) != null && getRole(player).isInnocent();
    }

    public void clearRoleMap() {
        this.roles.clear();
        setPsychosActive(0);
    }

    public void queueTrainReset() {
        if (TMM.isLobby) {
            ticksUntilNextResetAttempt = -1;
            return;
        }
        ticksUntilNextResetAttempt = 10;
    }

    public int getPsychosActive() {
        return psychosActive;
    }

    public boolean isPsychoActive() {
        return psychosActive > 0;
    }

    public int setPsychosActive(int psychosActive) {
        this.psychosActive = Math.max(0, psychosActive);
        this.sync();
        return this.psychosActive;
    }

    public GameMode getGameMode() {
        return gameMode == null ? TMMGameModes.MURDER : gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        this.sync();
    }

    public UUID getLooseEndWinner() {
        return this.looseEndWinner;
    }

    public void setLooseEndWinner(UUID looseEndWinner) {
        this.looseEndWinner = looseEndWinner;
        this.sync();
    }

    public boolean isLockedToSupporters() {
        return lockedToSupporters;
    }

    public void setLockedToSupporters(boolean lockedToSupporters) {
        this.lockedToSupporters = lockedToSupporters;
    }

    @Deprecated
    public GameFunctions.WinStatus getLastWinStatus() {
        return lastWinStatus;
    }

    @Deprecated
    public void setLastWinStatus(GameFunctions.WinStatus lastWinStatus) {
        this.lastWinStatus = lastWinStatus;
        this.sync();
    }

    public float getBackfireChance() {
        return backfireChance;
    }

    public void setBackfireChance(float backfireChance) {
        this.backfireChance = backfireChance;
        this.sync();
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer sp) {
        return true;
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag nbtCompound, HolderLookup.Provider wrapperLookup) {
        // this.lockedToSupporters = nbtCompound.getBoolean("LockedToSupporters");
        // this.enableWeights = nbtCompound.getBoolean("EnableWeights");
        this.canJump = nbtCompound.getBoolean("canJump");
        this.syncRole = nbtCompound.getBoolean("SyncRole");
        // if (!syncRole) {
        this.gameMode = TMMGameModes.GAME_MODES.get(ResourceLocation.parse(nbtCompound.getString("GameMode")));
        this.gameStatus = GameStatus.valueOf(nbtCompound.getString("GameStatus"));

        this.fade = nbtCompound.getInt("Fade");
        this.psychosActive = nbtCompound.getInt("PsychosActive");

        // this.backfireChance = nbtCompound.getFloat("BackfireChance");
        if (nbtCompound.contains("LooseEndWinner")) {
            this.looseEndWinner = nbtCompound.getUUID("LooseEndWinner");
        } else {
            this.looseEndWinner = null;
        }
        // }else {
        for (Role role : TMMRoles.ROLES.values()) {
            this.setRoles(uuidListFromNbt(nbtCompound, role.identifier().toString()), role);
            // }
            this.setSyncRole(false);
        }

    }

    private ArrayList<UUID> uuidListFromNbt(CompoundTag nbtCompound, String listName) {
        ArrayList<UUID> ret = new ArrayList<>();
        for (Tag e : nbtCompound.getList(listName, Tag.TAG_INT_ARRAY)) {
            ret.add(NbtUtils.loadUUID(e));
        }
        return ret;
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag nbtCompound, HolderLookup.Provider wrapperLookup) {
        // nbtCompound.putBoolean("LockedToSupporters", lockedToSupporters);
        // nbtCompound.putBoolean("EnableWeights", enableWeights);
        nbtCompound.putBoolean("SyncRole", syncRole);
        nbtCompound.putBoolean("canJump", canJump);
        // if (!this.syncRole) {
        nbtCompound.putString("GameMode", this.gameMode != null ? this.gameMode.identifier.toString() : "");
        nbtCompound.putString("GameStatus", this.gameStatus.toString());

        nbtCompound.putInt("Fade", fade);
        nbtCompound.putInt("PsychosActive", psychosActive);
        if (this.looseEndWinner != null)
            nbtCompound.putUUID("LooseEndWinner", this.looseEndWinner);

        // nbtCompound.putString("LastWinStatus", this.lastWinStatus.toString());
        // nbtCompound.putFloat("BackfireChance", backfireChance);
        // }
        // else {
        for (Role role : TMMRoles.ROLES.values()) {
            nbtCompound.put(role.identifier().toString(), nbtFromUuidList(getAllWithRole(role)));
        }
        this.setSyncRole(false);
        // }

    }

    private ListTag nbtFromUuidList(List<UUID> list) {
        ListTag ret = new ListTag();
        for (UUID player : list) {
            ret.add(NbtUtils.createUUID(player));
        }
        return ret;
    }

    @Override
    public void clientTick() {
        tickCommon();

        if (this.isRunning()) {
            if (gameMode == null)
                return;
            gameMode.tickClientGameLoop();
        }
    }

    @Override
    public void serverTick() {
        tickCommon();

        if (!(this.world instanceof ServerLevel serverWorld)) {
            return;
        }

        AreasWorldComponent areas = AreasWorldComponent.KEY.get(serverWorld);
        // 重置移动到游戏开始前
        // // attempt to reset the play area
        // if (--ticksUntilNextResetAttempt == 0) {
        //     if (GameFunctions.tryResetTrain(serverWorld)) {
        //         queueTrainReset();
        //     } else {
        //         // GameFunctions.getAllTaskPoints(serverWorld);
        //         ticksUntilNextResetAttempt = -1;
        //         OnTrainAreaHaveReseted.EVENT.invoker().onWorldHaveReseted(serverWorld);
        //     }
        // }

        // if not running and spectators or not in lobby reset them
        if (serverWorld.getServer().getTickCount() % 20 == 0) {
            for (ServerPlayer player : serverWorld.players()) {
                if (!isRunning() && (player.isSpectator()
                        && serverWorld.getServer().getProfilePermissions(player.getGameProfile()) < 2
                        || (GameFunctions.isPlayerAliveAndSurvival(player)
                                && areas.playArea.contains(player.position())))) {
                    GameFunctions.resetPlayerAfterGame(player);
                }
            }
        }

        if (serverWorld.getServer().overworld().equals(serverWorld)) {
            TrainWorldComponent trainComponent = TrainWorldComponent.KEY.get(serverWorld);

            // spectator limits
            if (trainComponent.getSpeed() > 0) {
                for (ServerPlayer player : serverWorld.players()) {
                    if (!GameFunctions.isPlayerAliveAndSurvival(player) && isBound()
                            && !GameFunctions.isPlayerCreative(player)) {
                        GameFunctions.limitPlayerToBox(player, areas.playArea);
                    }
                }
            }

            if (this.isRunning()) {
                for (ServerPlayer player : serverWorld.players()) {
                    if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                        // kill players who fell off the train
                        if (GameWorldComponent.KEY.get(world).getRole(player) == null) {
                            player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                        }
                        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                            isPlayerOutGameAreas(player, areas);
                        }

                        // put players with no role in spectator mode

                        // 调用角色的服务器端tick方法
                        dev.doctor4t.trainmurdermystery.api.RoleMethodDispatcher.callServerTick(player);
                    }
                }

                // Update total play time for active players
                for (ServerPlayer player : serverWorld.players()) {
                    if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                        PlayerStatsComponent.KEY.get(player).addPlayTime(1);
                    }
                }
                if (gameMode == null) {
                    gameStatus = GameStatus.STOPPING;
                    return;
                }

                // run game loop logic
                gameMode.tickServerGameLoop(serverWorld, this);

            }

            // if (serverWorld.getGameTime() % 40 == 0) {
            // this.sync();
            // }
        }
    }

    public static void isPlayerOutGameAreas(ServerPlayer player, AreasWorldComponent areas) {
        final var block = player.level()
                .getBlockState(new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ())).getBlock();
        final var block1 = player.level()
                .getBlockState(new BlockPos((int) player.getX(), (int) (player.getY() - 1), (int) player.getZ()))
                .getBlock();
        final var block2 = player.level()
                .getBlockState(new BlockPos((int) player.getX(), (int) (player.getY() - 2), (int) player.getZ()))
                .getBlock();
        if (player.getY() < areas.playArea.minY
                || !areas.canSwim && (block == Blocks.WATER && block1 == Blocks.WATER && block2 == Blocks.WATER)) {
            GameFunctions.killPlayer(player, false,
                    player.getLastAttacker() instanceof Player killerPlayer ? killerPlayer : null,
                    GameConstants.DeathReasons.FELL_OUT_OF_TRAIN);
        }
    }

    private void tickCommon() {
        // fade and start / stop game
        if (this.getGameStatus() == GameStatus.STARTING || this.getGameStatus() == GameStatus.STOPPING) {
            this.setFade(fade + 1);

            if (this.getFade() >= GameConstants.FADE_TIME + GameConstants.FADE_PAUSE) {
                if (world instanceof ServerLevel serverWorld) {
                    if (this.getGameStatus() == GameStatus.STARTING)
                        GameFunctions.initializeGame(serverWorld);
                    if (this.getGameStatus() == GameStatus.STOPPING)
                        GameFunctions.finalizeGame(serverWorld);
                }
            }
        } else if (this.getGameStatus() == GameStatus.ACTIVE || this.getGameStatus() == GameStatus.INACTIVE) {
            this.setFade(fade - 1);
        }

        if (this.isRunning()) {
            if (gameMode == null) {

                return;
            }
            gameMode.tickCommonGameLoop();
        }
    }

    public boolean canSeeKillerTeammate(Player player) {
        return getRole(player) != null && getRole(player).canSeeTeammateKiller();
    }

    public boolean isKillerTeamRole(Role role) {
        if (role == null)
            return false;
        if (role.canUseKiller())
            return true;
        if (role.isNeutralForKiller())
            return true;
        return false;
    }

    public boolean isKillerTeam(Player player) {
        if (player != null) {
            var role = this.getRole(player);
            if (role == null)
                return false;
            if (role.canUseKiller())
                return true;
            if (role.isNeutralForKiller())
                return true;
        }
        return false;
    }

    public static boolean isKillerTeamRoleStatic(Role role) {
        if (role == null)
            return false;
        if (role.canUseKiller())
            return true;
        if (role.isNeutralForKiller())
            return true;
        return false;
    }
}