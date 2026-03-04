package dev.doctor4t.trainmurdermystery.game;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.google.common.collect.Lists;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.block.FoodPlatterBlock;
import dev.doctor4t.trainmurdermystery.block.NeonPillarBlock;
import dev.doctor4t.trainmurdermystery.block.NeonTubeBlock;
import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.block.SprinklerBlock;
import dev.doctor4t.trainmurdermystery.block.ToggleableFacingLightBlock;
import dev.doctor4t.trainmurdermystery.block.TrimmedBedBlock;
import dev.doctor4t.trainmurdermystery.block.VentHatchBlock;
import dev.doctor4t.trainmurdermystery.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SprinklerBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.TrimmedBedBlockEntity;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions.BlockEntityInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ServerTaskInfoClasses {
    public static abstract class ServerTaskInfo {
        public boolean finished = false;
        public boolean cancelled = false;

        /**
         * Called every tick.
         * 
         * @return true for task finished.
         */
        public boolean onTick(MinecraftServer server) {
            return true;
        }

        /**
         * Called on task finished.
         */
        public void onFinished() {

        }
    }

    public static class AutoTrainResetTask extends ServerTaskInfo {
        int progress = 0;
        AreasWorldComponent area;
        int count = 0;
        private ServerLevel serverWorld;
        private GameMode gameMode;
        private int time;
        private int MAX_RESET_PER = 1;
        BlockPos backupMinPos;
        BlockPos backupMaxPos;
        BoundingBox backupTrainBox;
        BlockPos trainMinPos;
        BlockPos trainMaxPos;
        BoundingBox trainBox;
        int totalProgress = 0;
        BlockPos offsetBlockPos;

        public AutoTrainResetTask(AreasWorldComponent areas, ServerLevel world, GameMode gameMode, int gameStartTime) {
            if (TMMConfig.verboseTrainResetLogs) {
                TMM.LOGGER.info("Resetting train " + areas.mapName);
            }
            backupMinPos = BlockPos.containing(areas.getResetTemplateArea().getMinPosition());
            backupMaxPos = BlockPos.containing(areas.getResetTemplateArea().getMaxPosition());
            backupTrainBox = BoundingBox.fromCorners(backupMinPos, backupMaxPos);
            trainMinPos = BlockPos.containing(areas.getResetPasteArea().getMinPosition());
            trainMaxPos = trainMinPos.offset(backupTrainBox.getLength());
            trainBox = BoundingBox.fromCorners(trainMinPos, trainMaxPos);
            offsetBlockPos = new BlockPos(
                    trainBox.minX() - backupTrainBox.minX(), trainBox.minY() - backupTrainBox.minY(),
                    trainBox.minZ() - backupTrainBox.minZ());
            int xSize = trainBox.maxX() - trainBox.minX();
            int zSize = trainBox.maxZ() - trainBox.minZ();
            int flatSize = xSize * zSize;
            MAX_RESET_PER = 500 / flatSize;
            if (MAX_RESET_PER < 1)
                MAX_RESET_PER = 1;
            this.totalProgress = trainBox.maxY() - trainBox.minY() + 1;
            this.area = areas;
            this.progress = 0;
            this.serverWorld = world;
            this.gameMode = gameMode;
            this.time = gameStartTime;
        }

        public void resetBlock() {
            for (int i = 0; i < MAX_RESET_PER && this.progress < this.totalProgress; i++, this.progress++) {

                List<GameFunctions.BlockInfo> list = Lists.newArrayList();
                List<GameFunctions.BlockInfo> list2 = Lists.newArrayList();
                List<GameFunctions.BlockInfo> list3 = Lists.newArrayList();
                Deque<BlockPos> deque = Lists.newLinkedList();
                int nowY = backupTrainBox.minY() + this.progress;

                BoundingBox copyAreas = BoundingBox
                        .fromCorners(new BlockPos(backupTrainBox.minX(), nowY, backupTrainBox.minZ()),
                                new BlockPos(backupTrainBox.maxX(), nowY, backupTrainBox.maxZ()));
                                
                serverWorld.getBlockTicks().copyAreaFrom(serverWorld.getBlockTicks(), copyAreas, offsetBlockPos);

                for (int k = backupTrainBox.minZ(); k <= backupTrainBox.maxZ(); k++) {
                    {
                        for (int m = backupTrainBox.minX(); m <= backupTrainBox.maxX(); m++) {
                            BlockPos blockPos6 = new BlockPos(m, nowY, k);
                            BlockPos blockPos7 = blockPos6.offset(offsetBlockPos);
                            BlockInWorld cachedBlockPosition = new BlockInWorld(serverWorld, blockPos6, true);
                            BlockState blockState = cachedBlockPosition.getState();

                            BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos6);
                            if (blockEntity != null) {
                                BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                        blockEntity.saveCustomOnly(serverWorld.registryAccess()),
                                        blockEntity.components());
                                list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, blockEntityInfo));
                                deque.addLast(blockPos6);
                            } else if (!blockState.isSolidRender(serverWorld, blockPos6)
                                    && !blockState.isCollisionShapeFullBlock(serverWorld, blockPos6)) {
                                list3.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                                deque.addFirst(blockPos6);
                            } else {
                                list.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                                deque.addLast(blockPos6);
                            }
                        }
                    }
                }
                List<GameFunctions.BlockInfo> list4 = Lists.newArrayList();
                list4.addAll(list);
                list4.addAll(list2);
                list4.addAll(list3);
                List<GameFunctions.BlockInfo> list5 = Lists.reverse(list4);

                for (GameFunctions.BlockInfo blockInfo : list5) {
                    BlockEntity blockEntity3 = serverWorld.getBlockEntity(blockInfo.pos());
                    Clearable.tryClear(blockEntity3);
                    serverWorld.setBlock(blockInfo.pos(), Blocks.BARRIER.defaultBlockState(), Block.UPDATE_CLIENTS);
                }

                for (GameFunctions.BlockInfo blockInfo2 : list4) {
                    if (serverWorld.setBlock(blockInfo2.pos(), blockInfo2.state(), Block.UPDATE_CLIENTS)) {
                    }
                }

                for (GameFunctions.BlockInfo blockInfo2x : list2) {
                    BlockEntity blockEntity4 = serverWorld.getBlockEntity(blockInfo2x.pos());
                    if (blockInfo2x.blockEntityInfo() != null && blockEntity4 != null) {
                        blockEntity4.loadCustomOnly(blockInfo2x.blockEntityInfo().nbt(), serverWorld.registryAccess());
                        blockEntity4.setComponents(blockInfo2x.blockEntityInfo().components());
                        blockEntity4.setChanged();
                    }

                    serverWorld.setBlock(blockInfo2x.pos(), blockInfo2x.state(), Block.UPDATE_CLIENTS);
                }
            }
        }

        @Override
        public boolean onTick(MinecraftServer server) {
            count++;

            if (area.noReset) {
                TMM.LOGGER.info("NO RESET MAP!");
                return true;
            }
            if (this.progress >= this.totalProgress) {
                return true;
            }
            this.resetBlock();

            if (count % 10 == 1) {
                // 1s
                TMM.LOGGER.info("RESETING MAP: {}/{}", this.progress, this.totalProgress);

                this.serverWorld.players().forEach((p) -> {
                    p.displayClientMessage(
                            Component
                                    .translatable("message.tmm.reseting",
                                            String.format("%.0f", (this.progress / (float) this.totalProgress) * 100))
                                    .withStyle(ChatFormatting.GOLD),
                            true);
                });
            }
            if (this.progress >= this.totalProgress) {
                return true;
            }
            return false;
        }

        @Override
        public void onFinished() {
            this.serverWorld.players().forEach((p) -> {
                p.displayClientMessage(
                        Component
                                .translatable("message.tmm.reseting",
                                        "100")
                                .withStyle(ChatFormatting.GOLD),
                        true);
            });
            TMM.LOGGER.info("RESETING MAP FINISHED. STARTING THE GAME.");
            GameFunctions.trueStartGame(this.serverWorld, this.gameMode, this.time);
        }
    }

    public static class OnlySomeBlockResetTask extends ServerTaskInfo {
        ArrayList<BlockPos> blocks = null;
        int progress = 0;
        int totalProgress = 0;
        int count = 0;
        private ServerLevel world;
        private GameMode gameMode;
        private int time;
        private final int MAX_RESET_PER = 500;

        public OnlySomeBlockResetTask(ArrayList<BlockPos> points, ServerLevel world, GameMode gameMode,
                int gameStartTime) {
            this.blocks = new ArrayList<BlockPos>(points);
            this.progress = 0;
            this.totalProgress = this.blocks.size();
            this.world = world;
            this.gameMode = gameMode;
            this.time = gameStartTime;
        }

        public void resetBlock() {
            TMM.LOGGER.info("RESETING MAP: {}/{}", this.progress, this.totalProgress);
            ServerLevel serverWorld = this.world;
            ArrayList<GameFunctions.BlockInfo> list3 = new ArrayList<>(); // 仅更新方块状态
            ArrayList<GameFunctions.BlockInfo> list2 = new ArrayList<>();
            for (int i = 0; i <= MAX_RESET_PER && this.progress < this.totalProgress; i++, this.progress++) {
                BlockPos blockPos6 = blocks.get(this.progress);
                BlockPos blockPos7 = blockPos6;
                BlockInWorld cachedBlockPosition = new BlockInWorld(serverWorld, blockPos6, true);
                BlockState blockState = cachedBlockPosition.getState();

                // Check if the block is one of our door blocks
                if (blockState.getBlock() instanceof SmallDoorBlock) {
                    if (blockState.getValue(SmallDoorBlock.HALF).equals(DoubleBlockHalf.LOWER)) {
                        if (serverWorld.getBlockEntity(blockPos6) instanceof SmallDoorBlockEntity entity) {
                            entity.setBlasted(false);
                            entity.setJammed(0);
                            entity.setOpen(false);
                            String keyName = entity.getKeyName();
                            if (keyName == null)
                                keyName = "";
                            else if (keyName.endsWith(":")) {
                                keyName = "";
                            } else if (keyName.contains(":")) {
                                var arr = keyName.split(":");
                                if (arr.length > 0) {
                                    keyName = arr[arr.length - 1];
                                }
                            }
                            entity.setKeyName(keyName);
                            blockState = blockState.setValue(SmallDoorBlock.OPEN, false);
                            BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                    entity.saveCustomOnly(serverWorld.registryAccess()),
                                    entity.components());
                            list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, blockEntityInfo));
                        }
                    } else if (blockState.getValue(SmallDoorBlock.HALF).equals(DoubleBlockHalf.UPPER)) {
                        blockState = blockState.setValue(SmallDoorBlock.OPEN, false);
                        list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                    }
                } else if (blockState.getBlock() instanceof TrimmedBedBlock) {
                    if (blockState.getValue(TrimmedBedBlock.PART).equals(BedPart.HEAD)) {
                        if (serverWorld.getBlockEntity(blockPos6) instanceof TrimmedBedBlockEntity entity) {
                            entity.setHasScorpion(false, null);
                            blockState = blockState.setValue(TrimmedBedBlock.OCCUPIED, false);
                            BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                    entity.saveCustomOnly(serverWorld.registryAccess()),
                                    entity.components());
                            list3.add(new GameFunctions.BlockInfo(blockPos7, blockState, blockEntityInfo));
                            // deque.addLast(blockPos6); // Add to end to process last
                        }
                    }
                } else if (blockState.getBlock() instanceof FoodPlatterBlock) {
                    if (serverWorld.getBlockEntity(blockPos6) instanceof BeveragePlateBlockEntity entity) {
                        entity.setArmorer(null);
                        entity.setPoisoner(null);
                        BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                entity.saveCustomOnly(serverWorld.registryAccess()),
                                entity.components());
                        list3.add(new GameFunctions.BlockInfo(blockPos7, blockState, blockEntityInfo));
                    }
                } else if (blockState.getBlock() instanceof LecternBlock) {
                    if (serverWorld.getBlockEntity(blockPos6) instanceof LecternBlockEntity entity) {
                        BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                entity.saveCustomOnly(serverWorld.registryAccess()),
                                entity.components());
                        list3.add(new GameFunctions.BlockInfo(blockPos7, blockState, blockEntityInfo));
                    }
                } else if (blockState.getBlock() instanceof SprinklerBlock) {
                    if (serverWorld.getBlockEntity(blockPos6) instanceof SprinklerBlockEntity entity) {
                        entity.setPowered(false);
                        BlockEntityInfo blockEntityInfo = new BlockEntityInfo(
                                entity.saveCustomOnly(serverWorld.registryAccess()),
                                entity.components());
                        blockState = blockState.setValue(SprinklerBlock.POWERED, false);
                        list3.add(new GameFunctions.BlockInfo(blockPos7, blockState, blockEntityInfo));
                    }
                } else if (blockState.getBlock() instanceof NeonPillarBlock) {
                    blockState = blockState.setValue(NeonPillarBlock.ACTIVE, true);
                    blockState = blockState.setValue(NeonPillarBlock.LIT, true);
                    list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                } else if (blockState.getBlock() instanceof NeonTubeBlock) {
                    blockState = blockState.setValue(NeonTubeBlock.ACTIVE, true);
                    blockState = blockState.setValue(NeonTubeBlock.LIT, true);
                    list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                } else if (blockState.getBlock() instanceof NeonTubeBlock) {
                    blockState = blockState.setValue(NeonTubeBlock.ACTIVE, true);
                    blockState = blockState.setValue(NeonTubeBlock.LIT, true);
                    list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                } else if (blockState.getBlock() instanceof ToggleableFacingLightBlock) {
                    blockState = blockState.setValue(ToggleableFacingLightBlock.ACTIVE, true);
                    blockState = blockState.setValue(ToggleableFacingLightBlock.LIT, true);
                    list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                } else if (blockState.getBlock() instanceof VentHatchBlock) {
                    blockState = blockState.setValue(VentHatchBlock.OPEN, false);
                    list2.add(new GameFunctions.BlockInfo(blockPos7, blockState, null));
                }
            }

            List<GameFunctions.BlockInfo> list4 = Lists.newArrayList();
            list4.addAll(list2); // Only doors
            List<GameFunctions.BlockInfo> list_onlyBlockEntity = Lists.newArrayList();
            list_onlyBlockEntity.addAll(list2);
            list_onlyBlockEntity.addAll(list3);
            List<GameFunctions.BlockInfo> list6 = Lists.newArrayList();
            list6.addAll(list4);
            list6.addAll(list3);
            List<GameFunctions.BlockInfo> list5 = Lists.reverse(list6);

            // Clear only the door locations with barrier blocks
            for (GameFunctions.BlockInfo blockInfo : list5) {
                BlockEntity blockEntity3 = serverWorld.getBlockEntity(blockInfo.pos());
                Clearable.tryClear(blockEntity3);
            }
            for (GameFunctions.BlockInfo blockInfo : list4) {
                serverWorld.setBlock(blockInfo.pos(), Blocks.BARRIER.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            @SuppressWarnings("unused")
            int mx = 1;

            // Place the doors back
            for (GameFunctions.BlockInfo blockInfo2 : list4) {
                if (serverWorld.setBlock(blockInfo2.pos(), blockInfo2.state(), Block.UPDATE_CLIENTS)) {
                    mx++;
                }
            }

            // Restore block entities for doors
            for (GameFunctions.BlockInfo blockInfo2x : list_onlyBlockEntity) {
                BlockEntity blockEntity4 = serverWorld.getBlockEntity(blockInfo2x.pos());
                if (blockInfo2x.blockEntityInfo() != null && blockEntity4 != null) {
                    blockEntity4.loadCustomOnly(blockInfo2x.blockEntityInfo().nbt(), serverWorld.registryAccess());
                    blockEntity4.setComponents(blockInfo2x.blockEntityInfo().components());
                    blockEntity4.setChanged();
                }

            }
            for (GameFunctions.BlockInfo blockInfo2x : list5) {
                serverWorld.blockUpdated(blockInfo2x.pos(), blockInfo2x.state().getBlock());
            }
        }

        @Override
        public boolean onTick(MinecraftServer server) {
            count++;
            if (this.progress >= this.totalProgress) {
                return true;
            }
            this.resetBlock();

            if (count % 10 == 1) {
                // 1s
                this.world.players().forEach((p) -> {
                    p.displayClientMessage(
                            Component
                                    .translatable("message.tmm.reseting",
                                            String.format("%.0f", (this.progress / (float) this.totalProgress) * 100))
                                    .withStyle(ChatFormatting.GOLD),
                            true);
                });
            }
            if (this.progress >= this.totalProgress) {
                return true;
            }
            return false;
        }

        @Override
        public void onFinished() {
            this.world.players().forEach((p) -> {
                p.displayClientMessage(
                        Component
                                .translatable("message.tmm.reseting",
                                        "100")
                                .withStyle(ChatFormatting.GOLD),
                        true);
            });
            TMM.LOGGER.info("RESETING MAP FINISHED. STARTING THE GAME.");
            GameFunctions.trueStartGame(this.world, this.gameMode, this.time);
        }
    }
}
