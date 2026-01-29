package dev.doctor4t.trainmurdermystery;

import com.google.common.reflect.Reflection;

import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import dev.architectury.event.events.common.PlayerEvent.PlayerJoin;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.replay.ReplayApiInitializer;
import dev.doctor4t.trainmurdermystery.block.DoorPartBlock;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.command.*;
import dev.doctor4t.trainmurdermystery.command.argument.GameModeArgumentType;
import dev.doctor4t.trainmurdermystery.command.argument.TimeOfDayArgumentType;
import dev.doctor4t.trainmurdermystery.event.AFKEventHandler;
import dev.doctor4t.trainmurdermystery.event.EntityInteractionHandler;
import dev.doctor4t.trainmurdermystery.event.PlayerInteractionHandler;
import dev.doctor4t.trainmurdermystery.game.*;
import dev.doctor4t.trainmurdermystery.index.*;
import dev.doctor4t.trainmurdermystery.network.*;

import dev.doctor4t.trainmurdermystery.util.*;
import dev.upcraft.datasync.api.util.Entitlements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class TMM implements ModInitializer {
    public static final String MOD_ID = "trainmurdermystery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER;
    public static MurderGameMode GAME;
    public static TMMConfig CONFIG = new TMMConfig();
    public static GameReplayManager REPLAY_MANAGER;
    public static final Networking NETWORKING = new Networking();

    public static List<Predicate<Role>> canUseOtherPerson = new ArrayList<>();
    public static List<Predicate<Role>> canUseChatHud= new ArrayList<>();

    public static @NotNull ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        // Init config - must be called first to generate config file
        TMMConfig.init();

        // Init constants
        GameConstants.init();

        // Initialize waypoints
        dev.doctor4t.trainmurdermystery.util.WaypointInitUtil.initialize();

        // Initialize Replay API serializers
        ReplayApiInitializer.init();

        // Register event handlers
        PlayerInteractionHandler.register();
        EntityInteractionHandler.register();
        AFKEventHandler.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SERVER = server;
            GAME = new MurderGameMode(TMM.id("murder"));
            
            // 注册服务器tick事件以处理投票计时
            ServerTickEvents.START_SERVER_TICK.register(serv -> {
                dev.doctor4t.trainmurdermystery.voting.MapVotingManager.getInstance().tick();
            });
            REPLAY_MANAGER = new GameReplayManager(server);
            
            // 当服务器启动时，发送地图配置到所有在线玩家
            SyncMapConfigPayload.sendToAllPlayers();
        });

        // Registry initializers
        Reflection.initialize(TMMDataComponentTypes.class);
        TMMSounds.initialize();
        TMMEntities.initialize();
        TMMBlocks.initialize();
        TMMItems.initialize();
        TMMBlockEntities.initialize();
            

        TMMParticles.initialize();

        // Initialize network statistics
        NetworkStatistics.getInstance().initialize();

        // Register command argument types
        ArgumentTypeRegistry.registerArgumentType(id("timeofday"), TimeOfDayArgumentType.class, SingletonArgumentInfo.contextFree(TimeOfDayArgumentType::timeofday));
        ArgumentTypeRegistry.registerArgumentType(id("gamemode"), GameModeArgumentType.class, SingletonArgumentInfo.contextFree(GameModeArgumentType::gameMode));

        // Register commands
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            GiveRoomKeyCommand.register(dispatcher);
            StartCommand.register(dispatcher);
            StopCommand.register(dispatcher);
            EnableWeightsCommand.register(dispatcher);
            CheckWeightsCommand.register(dispatcher);
            ResetWeightsCommand.register(dispatcher);
            SetVisualCommand.register(dispatcher);
            ForceRoleCommand.register(dispatcher);
//            UpdateDoorsCommand.register(dispatcher);
            SetTimerCommand.register(dispatcher);
            SetMoneyCommand.register(dispatcher);
            SetAutoTrainResetCommand.register(dispatcher);
            SetBoundCommand.register(dispatcher);
            AutoStartCommand.register(dispatcher);
            LockToSupportersCommand.register(dispatcher);
            SetRoleCountCommand.register(dispatcher);
            ConfigCommand.register(dispatcher);
            SwitchMapCommand.register(dispatcher);
            ReloadReadyAreaCommand.register(dispatcher);
            EntityDataCommand.register(dispatcher);
            MoodChangeCommand.register(dispatcher);
            dev.doctor4t.trainmurdermystery.command.MapVoteCommand.register(dispatcher);
            dev.doctor4t.trainmurdermystery.command.CreateWaypointCommand.register(dispatcher);
            dev.doctor4t.trainmurdermystery.command.ToggleWaypointsCommand.register(dispatcher);
            AFKCommand.register(dispatcher);
            ShowStatsCommand.register(dispatcher);
            ShowSelectedMapUICommand.register(dispatcher);
            NetworkStatsCommand.register(dispatcher);
            ReloadMapConfigCommand.register(dispatcher);
        }));
        

//        // server lock to supporters
//        ServerPlayerEvents.JOIN.register(player -> {
//            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
//
//            // 优化：提前检查是否启用锁定，避免不必要的API调用
//            if (!gameWorldComponent.isLockedToSupporters()) {
//                if (REPLAY_MANAGER != null) {
//                    REPLAY_MANAGER.recordPlayerName(player);
//                    REPLAY_MANAGER.addEvent(GameReplayData.EventType.PLAYER_JOIN, null, player.getUUID(), null, null);
//                }
//                return;
//            }
//
//            // 服务器已锁定，需要验证支持者身份
//            DataSyncAPI.refreshAllPlayerData(player.getUUID()).thenRunAsync(() -> {
//                try {
//                    // 再次检查锁定状态（可能在异步期间已更改）
//                    if (GameWorldComponent.KEY.get(player.level()).isLockedToSupporters()) {
//                        // 检查玩家是否为支持者
//                        if (!isSupporter(player)) {
//                            LOGGER.info("Player {} attempted to join locked server (supporters only)", player.getName().getString());
//                            player.connection.disconnect(Component.translatable("Server is reserved to doctor4t supporters."));
//                            return;
//                        }
//                    }
//
//                    // 支持者或锁定已解除，允许加入
//                    if (REPLAY_MANAGER != null) {
//                        REPLAY_MANAGER.recordPlayerName(player);
//                        REPLAY_MANAGER.addEvent(GameReplayData.EventType.PLAYER_JOIN, null, player.getUUID(), null, null);
//                    }
//                } catch (Exception e) {
//                    LOGGER.error("Error checking supporter status for player {}", player.getName().getString(), e);
//                }
//            }, player.level().getServer());
//
//            // gameWorldComponent.addPlayer(player); // Removed as method does not exist
//        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(handler.player.level());
            if (gameWorldComponent.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE) {
                // gameWorldComponent.removePlayer(handler.player); // Removed as method does not exist
            }
            if (REPLAY_MANAGER != null) {
                REPLAY_MANAGER.addEvent(GameReplayData.EventType.PLAYER_LEAVE, null, handler.player.getUUID(), null, null);
            }
        });

        // 注册地图配置同步包
        PayloadTypeRegistry.playS2C().register(SyncMapConfigPayload.ID, SyncMapConfigPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ShootMuzzleS2CPayload.ID, ShootMuzzleS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PoisonUtils.PoisonOverlayPayload.ID, PoisonUtils.PoisonOverlayPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GunDropPayload.ID, GunDropPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TaskCompletePayload.ID, TaskCompletePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnnounceWelcomePayload.ID, AnnounceWelcomePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnnounceEndingPayload.ID, AnnounceEndingPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ReplayPayload.ID, ReplayPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SecurityCameraModePayload.ID, SecurityCameraModePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowStatsPayload.ID, ShowStatsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowSelectedMapUIPayload.ID, ShowSelectedMapUIPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CloseUiPayload.ID, CloseUiPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.doctor4t.trainmurdermystery.network.packet.SyncWaypointsPacket.ID, dev.doctor4t.trainmurdermystery.network.packet.SyncWaypointsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.doctor4t.trainmurdermystery.network.packet.SyncWaypointVisibilityPacket.ID, dev.doctor4t.trainmurdermystery.network.packet.SyncWaypointVisibilityPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(dev.doctor4t.trainmurdermystery.network.packet.SyncSpecificWaypointVisibilityPacket.ID, dev.doctor4t.trainmurdermystery.network.packet.SyncSpecificWaypointVisibilityPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(KnifeStabPayload.ID, KnifeStabPayload.CODEC);

        // 新加的，出问题直接删下面这一行
        PayloadTypeRegistry.playC2S().register(ReplayPayload.ID, ReplayPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(GunShootPayload.ID, GunShootPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StoreBuyPayload.ID, StoreBuyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(NoteEditPayload.ID, NoteEditPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(dev.doctor4t.trainmurdermystery.network.VoteForMapPayload.ID, dev.doctor4t.trainmurdermystery.network.VoteForMapPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KnifeStabPayload.ID, new KnifeStabPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(GunShootPayload.ID, new GunShootPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(StoreBuyPayload.ID, new StoreBuyPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(NoteEditPayload.ID, new NoteEditPayload.Receiver());
        ServerPlayNetworking.registerGlobalReceiver(dev.doctor4t.trainmurdermystery.network.VoteForMapPayload.ID, (payload, context) -> {
            dev.doctor4t.trainmurdermystery.network.VoteForMapPayload.Handler.handle(payload, context.player());
        });


        
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            SyncMapConfigPayload.sendToPlayer(newPlayer);
        });

        Scheduler.init();
    }


    public static boolean isSkyVisibleAdjacent(@NotNull Entity player) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos playerPos = BlockPos.containing(player.getEyePosition());
        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {

                mutable.set(playerPos.getX() + x, playerPos.getY(), playerPos.getZ() + z);
                final var chunkPos = player.chunkPosition();
                final var chunk = player.level().getChunk(chunkPos.x, chunkPos.z);
                final var i = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING).getFirstAvailable(mutable.getX()&15, mutable.getZ()&15)-1;
                if (i< player.getY()+3) {
                    return !(player.level().getBlockState(playerPos).getBlock() instanceof DoorPartBlock);
                }
            }
        }
        return false;
    }

    public static boolean isExposedToWind(@NotNull Entity player) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos playerPos = BlockPos.containing(player.getEyePosition());
        for (int x = 0; x <= 10; x++) {
            mutable.set(playerPos.getX() - x, player.getEyePosition().y(), playerPos.getZ());
            if (!player.level().canSeeSky(mutable)) {
                return false;
            }
        }
        return true;
    }

    public static final ResourceLocation COMMAND_ACCESS = id("commandaccess");

    public static int executeSupporterCommand(CommandSourceStack source, Runnable runnable) {
        ServerPlayer player = source.getPlayer();
        if (player == null || !player.getClass().equals(ServerPlayer.class)) return 0;
        runnable.run();
        return 1;

    }

    public static @NotNull Boolean isSupporter(Player player) {
        Optional<Entitlements> entitlements = Entitlements.token().get(player.getUUID());
        return entitlements.map(value -> value.keys().stream().anyMatch(identifier -> identifier.equals(COMMAND_ACCESS))).orElse(false);
    }

    public static boolean isPlayerInGame(Player player) {
        return GameFunctions.isPlayerAliveAndSurvival(player);
    }

    public static class Networking {
        public void sendToAllPlayers(CustomPacketPayload packet) {
            if (SERVER != null) {
                for (ServerPlayer player : SERVER.getPlayerList().getPlayers()) {
                    PacketTracker.sendToClient(player, packet);
                }
            }
        }
    }
}
