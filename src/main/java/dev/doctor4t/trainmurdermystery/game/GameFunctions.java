package dev.doctor4t.trainmurdermystery.game;

import com.google.common.collect.Lists;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.cca.*;
import dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin;
import dev.doctor4t.trainmurdermystery.entity.FirecrackerEntity;
import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.event.AllowPlayerDeath;
import dev.doctor4t.trainmurdermystery.event.ShouldDropOnDeath;
import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.AnnounceEndingPayload;
import dev.doctor4t.trainmurdermystery.util.ReplayPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

public class GameFunctions {

    public static void limitPlayerToBox(ServerPlayer player, AABB box) {
        Vec3 playerPos = player.position();

        if (!box.contains(playerPos)) {
            double x = playerPos.x();
            double y = playerPos.y();
            double z = playerPos.z();

            if (z < box.minZ) {
                z = box.minZ;
            }
            if (z > box.maxZ) {
                z = box.maxZ;
            }

            if (y < box.minY) {
                y = box.minY;
            }
            if (y > box.maxY) {
                y = box.maxY;
            }

            if (x < box.minX) {
                x = box.minX;
            }
            if (x > box.maxX) {
                x = box.maxX;
            }

            player.teleportTo(x, y, z);
        }
    }

    public static void startGame(ServerLevel world, GameMode gameMode, int time) {
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        AreasWorldComponent areas = AreasWorldComponent.KEY.get(world);
        int playerCount = Math.toIntExact(world.players().stream().filter(serverPlayerEntity -> (areas.getReadyArea().contains(serverPlayerEntity.position()))).count());
        game.setGameMode(gameMode);
        GameTimeComponent.KEY.get(world).setResetTime(time);

        if (playerCount >= gameMode.minPlayerCount) {
            game.setGameStatus(GameWorldComponent.GameStatus.STARTING);
        } else {
            for (ServerPlayer player : world.players()) {
                player.displayClientMessage(Component.translatable("game.start_error.not_enough_players", gameMode.minPlayerCount), true);
            }
        }
    }

    public static void stopGame(ServerLevel world) {
        GameWorldComponent component = GameWorldComponent.KEY.get(world);
        component.setGameStatus(GameWorldComponent.GameStatus.STOPPING);
    }

    public static void initializeGame(ServerLevel serverWorld) {
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(serverWorld);
        List<ServerPlayer> readyPlayerList = getReadyPlayerList(serverWorld);

        serverWorld.setWeatherParameters(0,-1, true, true);
        baseInitialize(serverWorld, gameComponent, readyPlayerList);
        TMM.REPLAY_MANAGER.initializeReplay(readyPlayerList, gameComponent.getRoles());
        gameComponent.getGameMode().initializeGame(serverWorld, gameComponent, readyPlayerList);
        // Update replay with actual roles after assignment
        TMM.REPLAY_MANAGER.updateRolesFromComponent(gameComponent);
        
        // Set game status to ACTIVE after roles are assigned
        gameComponent.setGameStatus(GameWorldComponent.GameStatus.ACTIVE);
        gameComponent.sync();
    }

    public static Vec3 getSpawnPos(AreasWorldComponent areas, int room){
        // Try to get position from configured room positions
        Vec3 configuredPos = areas.getRoomPosition(room);
        if (configuredPos != null) {
            return configuredPos;
        }
        
        // Fallback to default positions based on room count
        int roomCount = areas.getRoomCount();
        if (roomCount >= 7) {
            if (room == 1) {
                return new Vec3(116, 122, -539);
            } else if (room == 2) {
                return new Vec3(124, 122, -534);
            } else if (room == 3) {
                return new Vec3(131, 122, -534);
            } else if (room == 4) {
                return new Vec3(144, 122, -540);
            } else if (room == 5) {
                return new Vec3(119, 128, -537);
            } else if (room == 6) {
                return new Vec3(132, 128, -536);
            } else if (room == 7) {
                return new Vec3(146, 128, -537);
            }
        } else if (roomCount >= 4) {
            // Handle 4-6 rooms
            switch (room) {
                case 1: return new Vec3(116, 122, -539);
                case 2: return new Vec3(124, 122, -534);
                case 3: return new Vec3(131, 122, -534);
                case 4: return new Vec3(144, 122, -540);
            }
        } else if (roomCount >= 2) {
            // Handle 2-3 rooms
            switch (room) {
                case 1: return new Vec3(116, 122, -539);
                case 2: return new Vec3(131, 122, -534);
                case 3: return new Vec3(144, 122, -540);
            }
        } else if (roomCount == 1) {
            // Handle single room
            return new Vec3(131, 122, -534);
        }
        return null;
    }
    
    public static Map<UUID, Integer> roomToPlayer = new HashMap<>();
    private static void baseInitialize(ServerLevel serverWorld, GameWorldComponent gameComponent, List<ServerPlayer> players) {
        AreasWorldComponent areas = AreasWorldComponent.KEY.get(serverWorld);

        TrainWorldComponent.KEY.get(serverWorld).reset();
        WorldBlackoutComponent.KEY.get(serverWorld).reset();

        serverWorld.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, serverWorld.getServer());
        serverWorld.getGameRules().getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(9999, serverWorld.getServer());
        serverWorld.getServer().setDifficulty(Difficulty.PEACEFUL, true);

        // dismount all players as it can cause issues
        for (ServerPlayer player : serverWorld.players()) {
            player.removeVehicle();
        }

        // teleport players to play area


        // teleport non playing players
        for (ServerPlayer player : serverWorld.getPlayers(serverPlayerEntity -> !players.contains(serverPlayerEntity))) {
            player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);

            AreasWorldComponent.PosWithOrientation spectatorSpawnPos = areas.getSpectatorSpawnPos();
            player.teleportTo(serverWorld, spectatorSpawnPos.pos.x(), spectatorSpawnPos.pos.y(), spectatorSpawnPos.pos.z(), spectatorSpawnPos.yaw, spectatorSpawnPos.pitch);
        }

        // clear items, clear previous game data
        for (ServerPlayer serverPlayerEntity : players) {
            serverPlayerEntity.getInventory().clearContent();
            PlayerMoodComponent.KEY.get(serverPlayerEntity).reset();
            PlayerShopComponent.KEY.get(serverPlayerEntity).reset();
            PlayerPoisonComponent.KEY.get(serverPlayerEntity).reset();
            PlayerPsychoComponent.KEY.get(serverPlayerEntity).reset();
            PlayerNoteComponent.KEY.get(serverPlayerEntity).reset();
            PlayerShopComponent.KEY.get(serverPlayerEntity).reset();
            TrainVoicePlugin.resetPlayer(serverPlayerEntity.getUUID());

            // remove item cooldowns
            HashSet<Item> copy = new HashSet<>(serverPlayerEntity.getCooldowns().cooldowns.keySet());
            for (Item item : copy) serverPlayerEntity.getCooldowns().removeCooldown(item);
        }
        gameComponent.clearRoleMap();
        GameTimeComponent.KEY.get(serverWorld).reset();

        // reset train
        gameComponent.queueTrainReset();

        // select rooms
        Collections.shuffle(players);
        int roomNumber = 0;
        int roomCount = areas.getRoomCount(); // Get room count from config
        for (ServerPlayer serverPlayerEntity : players) {
            ItemStack itemStack = new ItemStack(TMMItems.KEY);
            roomNumber = roomNumber % roomCount + 1;
            int finalRoomNumber = roomNumber;
            itemStack.update(DataComponents.LORE, ItemLore.EMPTY, component -> new ItemLore(Component.literal("Room " + finalRoomNumber).toFlatList(Style.EMPTY.withItalic(false).withColor(0xFF8C00))));
            serverPlayerEntity.addItem(itemStack);
            roomToPlayer.put(serverPlayerEntity.getUUID(), finalRoomNumber);

            // give letter
            ItemStack letter = new ItemStack(TMMItems.LETTER);

            letter.set(DataComponents.ITEM_NAME, Component.translatable(letter.getDescriptionId()));
            int letterColor = 0xC5AE8B;
            String tipString = "tip.letter.";
            letter.update(DataComponents.LORE, ItemLore.EMPTY, component -> {
                        List<Component> text = new ArrayList<>();
                        UnaryOperator<Style> stylizer = style -> style.withItalic(false).withColor(letterColor);

                        Component displayName = serverPlayerEntity.getDisplayName();
                        String string = displayName != null ? displayName.getString() : serverPlayerEntity.getName().getString();
                        if (string.charAt(string.length() - 1) == '\uE780') { // remove ratty supporter icon
                            string = string.substring(0, string.length() - 1);
                        }

                        text.add(Component.translatable(tipString + "name", string).withStyle(style -> style.withItalic(false).withColor(0xFFFFFF)));
                        text.add(Component.translatable(tipString + "room").withStyle(stylizer));
                        text.add(Component.translatable(tipString + "tooltip1",
                                Component.translatable(tipString + "room." + switch (finalRoomNumber) {
                                    case 1 -> "grand_suite";
                                    case 2, 3 -> "cabin_suite";
                                    default -> "twin_cabin";
                                }).getString()
                        ).withStyle(stylizer));
                        text.add(Component.translatable(tipString + "tooltip2").withStyle(stylizer));

                        return new ItemLore(text);
                    }
            );
            serverPlayerEntity.addItem(letter);
        }
        for (ServerPlayer player : players) {
            player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
            //
            Vec3 pos = getSpawnPos(areas, roomToPlayer.getOrDefault(player.getUUID(), 1));
            if (pos != null) {
                player.teleportTo(pos.x(), pos.y() + 1, pos.z());
            }
            else {
                Vec3 pos1 = player.position().add(areas.getPlayAreaOffset());
                player.teleportTo(pos1.x(), pos1.y() + 1, pos1.z());
            }
        }
        // Don't set game status to ACTIVE here - it will be set after roles are assigned in initializeGame()
    }

    private static List<ServerPlayer> getReadyPlayerList(ServerLevel serverWorld) {
        AreasWorldComponent areas =AreasWorldComponent.KEY.get(serverWorld);
        return serverWorld.getPlayers(serverPlayerEntity -> areas.getReadyArea().contains(serverPlayerEntity.position()));
    }

    public static void finalizeGame(ServerLevel world) {
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(world);
       var areasWorldComponent = AreasWorldComponent.KEY.get(world);
        areasWorldComponent.loadFromFile();
        world.setDayTime(18000);
        gameComponent.getGameMode().finalizeGame(world, gameComponent);
        TMM.REPLAY_MANAGER.finalizeReplay(gameComponent.getLastWinStatus());

        // Show replay to all players
        for (ServerPlayer player : world.players()) {
            TMM.REPLAY_MANAGER.showReplayToPlayer(player);
        }

        WorldBlackoutComponent.KEY.get(world).reset();
        TrainWorldComponent trainComponent = TrainWorldComponent.KEY.get(world);
        trainComponent.setSpeed(0);
        trainComponent.setTimeOfDay(TrainWorldComponent.TimeOfDay.DAY);

        // discard all player bodies
        for (PlayerBodyEntity body : world.getEntities(TMMEntities.PLAYER_BODY, playerBodyEntity -> true)) body.discard();
        for (FirecrackerEntity entity : world.getEntities(TMMEntities.FIRECRACKER, entity -> true)) entity.discard();
        for (NoteEntity entity : world.getEntities(TMMEntities.NOTE, entity -> true)) entity.discard();

        // reset all players
        for (ServerPlayer player : world.players()) {
            resetPlayer(player);
        }

        // reset game component
        GameTimeComponent.KEY.get(world).reset();
        gameComponent.clearRoleMap();
        gameComponent.setGameStatus(GameWorldComponent.GameStatus.INACTIVE);
        trainComponent.setTime(0);
        gameComponent.sync();
    }

    public static void resetPlayer(ServerPlayer player) {
        ServerPlayNetworking.send(player, new AnnounceEndingPayload());
        GameReplay replay = TMM.REPLAY_MANAGER.getCurrentReplay();
        if (replay != null) {
            ServerPlayNetworking.send(player, new ReplayPayload(replay));
        }
        player.removeVehicle();
        player.getInventory().clearContent();
        PlayerMoodComponent.KEY.get(player).reset();
        PlayerShopComponent.KEY.get(player).reset();
        PlayerPoisonComponent.KEY.get(player).reset();
        PlayerPsychoComponent.KEY.get(player).reset();
        PlayerNoteComponent.KEY.get(player).reset();
        TrainVoicePlugin.resetPlayer(player.getUUID());

        player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
        player.stopSleeping();
        AreasWorldComponent.PosWithOrientation spawnPos = AreasWorldComponent.KEY.get(player.level()).getSpawnPos();
        DimensionTransition teleportTarget = new DimensionTransition(player.serverLevel(), spawnPos.pos, Vec3.ZERO, spawnPos.yaw, spawnPos.pitch, DimensionTransition.DO_NOTHING);
        player.changeDimension(teleportTarget);
    }

    public static boolean isPlayerEliminated(Player player) {
        return player == null || !player.isAlive() || player.isCreative() || player.isSpectator();
    }

    @SuppressWarnings("unused")
    public static void killPlayer(Player victim, boolean spawnBody, @Nullable Player killer) {
        killPlayer(victim, spawnBody, killer, GameConstants.DeathReasons.GENERIC);
    }

    public static void killPlayer(Player victim, boolean spawnBody, @Nullable Player killer, ResourceLocation deathReason) {
        PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(victim);

        if (victim instanceof ServerPlayer serverVictim) {
            TMM.REPLAY_MANAGER.recordPlayerKill(killer != null ? killer.getUUID() : null, serverVictim.getUUID(), deathReason);
        }

        // Check if victim has a role assigned - if not, skip role-dependent logic
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
        if (gameWorldComponent.getRole(victim) == null) {
            // Player doesn't have a role (game not started or joined mid-game), don't kill them
            return;
        }

        if (!AllowPlayerDeath.EVENT.invoker().allowDeath(victim, deathReason)) return;
        if (component.getPsychoTicks() > 0) {
            if (component.getArmour() > 0) {
                component.setArmour(component.getArmour() - 1);
                component.sync();
                victim.playNotifySound(TMMSounds.ITEM_PSYCHO_ARMOUR, SoundSource.MASTER, 5F, 1F);
                return;
            } else {
                component.stopPsycho();
            }
        }

        if (victim instanceof ServerPlayer serverPlayerEntity && isPlayerAliveAndSurvival(serverPlayerEntity)) {
            serverPlayerEntity.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        } else {
            return;
        }

        if (killer != null && GameWorldComponent.KEY.get(killer.level()).canUseKillerFeatures(killer)) {
            PlayerShopComponent.KEY.get(killer).addToBalance(GameConstants.getMoneyPerKill());

            // replenish derringer
            for (List<ItemStack> list : killer.getInventory().compartments) {
                for (ItemStack stack : list) {
                    Boolean used = stack.get(TMMDataComponentTypes.USED);
                    if (stack.is(TMMItems.DERRINGER) && used != null && used) {
                        stack.set(TMMDataComponentTypes.USED, false);
                        killer.playNotifySound(TMMSounds.ITEM_DERRINGER_RELOAD, SoundSource.PLAYERS, 1.0f, 1.0f);
                    }
                }
            }
        }

        PlayerMoodComponent.KEY.get(victim).reset();

        if (spawnBody) {
            PlayerBodyEntity body = TMMEntities.PLAYER_BODY.create(victim.level());
            if (body != null) {
                body.setPlayerUuid(victim.getUUID());
                Vec3 spawnPos = victim.position().add(victim.getLookAngle().normalize().scale(1));
                body.moveTo(spawnPos.x(), victim.getY(), spawnPos.z(), victim.getYHeadRot(), 0f);
                body.setYRot(victim.getYHeadRot());
                body.setYHeadRot(victim.getYHeadRot());
                victim.level().addFreshEntity(body);
            }
        }

        for (List<ItemStack> list : victim.getInventory().compartments) {
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = list.get(i);
                if (shouldDropOnDeath(stack)) {
                    victim.drop(stack, true, false);
                    list.set(i, ItemStack.EMPTY);
                }
            }
        }

        if (gameWorldComponent.isInnocent(victim)) {
            GameTimeComponent.KEY.get(victim.level()).addTime(GameConstants.TIME_ON_CIVILIAN_KILL);
        }

        TrainVoicePlugin.addPlayer(victim.getUUID());
    }

    public static boolean shouldDropOnDeath(@NotNull ItemStack stack) {
        return !stack.isEmpty() && (stack.is(TMMItems.REVOLVER)|| stack.is(Items.SPYGLASS) || ShouldDropOnDeath.EVENT.invoker().shouldDrop(stack));
    }

    public static boolean isPlayerAliveAndSurvival(Player player) {
        return player != null && !player.isSpectator() && !player.isCreative();
    }

    public static boolean isPlayerSpectatingOrCreative(Player player) {
        return player != null && (player.isSpectator() || player.isCreative());
    }

    record BlockEntityInfo(CompoundTag nbt, DataComponentMap components) {
    }

    record BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo) {
    }

    enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean allowsOverlap;

        Mode(final boolean allowsOverlap) {
            this.allowsOverlap = allowsOverlap;
        }

        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }
    }

    // returns whether another reset should be attempted
    public static boolean tryResetTrain(ServerLevel serverWorld) {

        if (!TMMConfig.enableAutoTrainReset) {
            return false;
        }
        
        if (serverWorld.getServer().overworld().equals(serverWorld)) {
            AreasWorldComponent areas = AreasWorldComponent.KEY.get(serverWorld);
            if (TMMConfig.verboseTrainResetLogs) {
                TMM.LOGGER.info("Resetting train" + areas.toString());
            }
            BlockPos backupMinPos = BlockPos.containing(areas.getResetTemplateArea().getMinPosition());
            BlockPos backupMaxPos = BlockPos.containing(areas.getResetTemplateArea().getMaxPosition());
            BoundingBox backupTrainBox = BoundingBox.fromCorners(backupMinPos, backupMaxPos);
            BlockPos trainMinPos = BlockPos.containing(areas.getResetPasteArea().getMinPosition());
            BlockPos trainMaxPos = trainMinPos.offset(backupTrainBox.getLength());
            BoundingBox trainBox = BoundingBox.fromCorners(trainMinPos, trainMaxPos);

            //Mode mode = Mode.FORCE;


            if (!serverWorld.hasChunksAt(backupMinPos, backupMaxPos) || !serverWorld.hasChunksAt(trainMinPos, trainMaxPos)) {

                int backupChunkMinX = backupMinPos.getX() >> 4;
                int backupChunkMinZ = backupMinPos.getZ() >> 4;
                int backupChunkMaxX = backupMaxPos.getX() >> 4;
                int backupChunkMaxZ = backupMaxPos.getZ() >> 4;
                int trainChunkMinX = trainMinPos.getX() >> 4;
                int trainChunkMinZ = trainMinPos.getZ() >> 4;
                int trainChunkMaxX = trainMaxPos.getX() >> 4;
                int trainChunkMaxZ = trainMaxPos.getZ() >> 4;
                
                if (TMMConfig.verboseTrainResetLogs) {
                    TMM.LOGGER.info("Train reset: Loading chunks - Template: ({}, {}) to ({}, {}), Paste: ({}, {}) to ({}, {})",
                        backupChunkMinX, backupChunkMinZ, backupChunkMaxX, backupChunkMaxZ,
                        trainChunkMinX, trainChunkMinZ, trainChunkMaxX, trainChunkMaxZ);
                }
                
                // Force load the required chunks
                for (int x = backupChunkMinX; x <= backupChunkMaxX; x++) {
                    for (int z = backupChunkMinZ; z <= backupChunkMaxZ; z++) {
                        serverWorld.getChunk(x, z);
                    }
                }
                for (int x = trainChunkMinX; x <= trainChunkMaxX; x++) {
                    for (int z = trainChunkMinZ; z <= trainChunkMaxZ; z++) {
                        serverWorld.getChunk(x, z);
                    }
                }

                if (TMMConfig.verboseTrainResetLogs) {
                    TMM.LOGGER.info("Train reset: Chunks loaded, attempting reset.");
                }
                // Continue with the reset after loading chunks
            }
            
            if (serverWorld.hasChunksAt(backupMinPos, backupMaxPos) && serverWorld.hasChunksAt(trainMinPos, trainMaxPos)) {
                List<BlockInfo> list = Lists.newArrayList();
                List<BlockInfo> list2 = Lists.newArrayList();
                List<BlockInfo> list3 = Lists.newArrayList();
                Deque<BlockPos> deque = Lists.newLinkedList();
                BlockPos blockPos5 = new BlockPos(
                        trainBox.minX() - backupTrainBox.minX(), trainBox.minY() - backupTrainBox.minY(), trainBox.minZ() - backupTrainBox.minZ()
                );

                for (int k = backupTrainBox.minZ(); k <= backupTrainBox.maxZ(); k++) {
                    for (int l = backupTrainBox.minY(); l <= backupTrainBox.maxY(); l++) {
                        for (int m = backupTrainBox.minX(); m <= backupTrainBox.maxX(); m++) {
                            BlockPos blockPos6 = new BlockPos(m, l, k);
                            BlockPos blockPos7 = blockPos6.offset(blockPos5);
                            BlockInWorld cachedBlockPosition = new BlockInWorld(serverWorld, blockPos6, false);
                            BlockState blockState = cachedBlockPosition.getState();

                            BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos6);
                            if (blockEntity != null) {
                                BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                        blockEntity.saveCustomOnly(serverWorld.registryAccess()), blockEntity.components()
                                );
                                list2.add(new BlockInfo(blockPos7, blockState, blockEntityInfo));
                                deque.addLast(blockPos6);
                            } else if (!blockState.isSolidRender(serverWorld, blockPos6) && !blockState.isCollisionShapeFullBlock(serverWorld, blockPos6)) {
                                list3.add(new BlockInfo(blockPos7, blockState, null));
                                deque.addFirst(blockPos6);
                            } else {
                                list.add(new BlockInfo(blockPos7, blockState, null));
                                deque.addLast(blockPos6);
                            }
                        }
                    }
                }

                List<BlockInfo> list4 = Lists.newArrayList();
                list4.addAll(list);
                list4.addAll(list2);
                list4.addAll(list3);
                List<BlockInfo> list5 = Lists.reverse(list4);

                for (BlockInfo blockInfo : list5) {
                    BlockEntity blockEntity3 = serverWorld.getBlockEntity(blockInfo.pos);
                    Clearable.tryClear(blockEntity3);
                    serverWorld.setBlock(blockInfo.pos, Blocks.BARRIER.defaultBlockState(), Block.UPDATE_CLIENTS);
                }

                int mx = 0;

                for (BlockInfo blockInfo2 : list4) {
                    if (serverWorld.setBlock(blockInfo2.pos, blockInfo2.state, Block.UPDATE_CLIENTS)) {
                        mx++;
                    }
                }

                for (BlockInfo blockInfo2x : list2) {
                    BlockEntity blockEntity4 = serverWorld.getBlockEntity(blockInfo2x.pos);
                    if (blockInfo2x.blockEntityInfo != null && blockEntity4 != null) {
                        blockEntity4.loadCustomOnly(blockInfo2x.blockEntityInfo.nbt, serverWorld.registryAccess());
                        blockEntity4.setComponents(blockInfo2x.blockEntityInfo.components);
                        blockEntity4.setChanged();
                    }

                    serverWorld.setBlock(blockInfo2x.pos, blockInfo2x.state, Block.UPDATE_CLIENTS);
                }

                for (BlockInfo blockInfo2x : list5) {
                    serverWorld.blockUpdated(blockInfo2x.pos, blockInfo2x.state.getBlock());
                }

                serverWorld.getBlockTicks().copyAreaFrom(serverWorld.getBlockTicks(), backupTrainBox, blockPos5);
                if (mx == 0) {
                    if (TMMConfig.verboseTrainResetLogs) {
                        TMM.LOGGER.info("Train reset failed: No blocks copied. Queueing another attempt.");
                    }
                    return true;
                }
            } else {
                if (TMMConfig.verboseTrainResetLogs) {
                    TMM.LOGGER.info("Train reset failed: Clone positions not loaded. Queueing another attempt.");
                }
                return true;
            }

            // discard all player bodies and items
            for (PlayerBodyEntity body : serverWorld.getEntities(TMMEntities.PLAYER_BODY, playerBodyEntity -> true)) {
                body.discard();
            }
            for (ItemEntity item : serverWorld.getEntities(EntityType.ITEM, playerBodyEntity -> true)) {
                item.discard();
            }
            for (FirecrackerEntity entity : serverWorld.getEntities(TMMEntities.FIRECRACKER, entity -> true)) entity.discard();
            for (NoteEntity entity : serverWorld.getEntities(TMMEntities.NOTE, entity -> true)) entity.discard();


            TMM.LOGGER.info("Train reset successful.");
            return false;
        }
        return false;
    }

    public static int getReadyPlayerCount(Level world) {
        List<? extends Player> players = world.players();
        AreasWorldComponent areas = AreasWorldComponent.KEY.get(world);
        return Math.toIntExact(players.stream().filter(p -> areas.getReadyArea().contains(p.position())).count());
    }

    public enum WinStatus {
        NONE, KILLERS, PASSENGERS, TIME, LOOSE_END,GAMBLER
    }
}