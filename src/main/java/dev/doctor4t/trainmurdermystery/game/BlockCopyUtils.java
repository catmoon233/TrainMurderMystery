package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BlockCopyUtils {

    /**
     * 将源区域内的所有方块（包括方块实体）复制到目标偏移位置，不触发任何方块更新。
     *
     * @param world     服务端世界
     * @param sourceBox 源区域边界框（包含两个端点）
     * @param offset    偏移量，目标位置 = 源位置 + offset
     */
    public static void copyAreaWithoutUpdates(ServerLevel world, BoundingBox sourceBox, BlockPos offset) {
        // 遍历源区域内的每一个方块坐标
        for (BlockPos sourcePos : BlockPos.betweenClosed(sourceBox.minX(), sourceBox.minY(), sourceBox.minZ(),
                sourceBox.maxX(), sourceBox.maxY(), sourceBox.maxZ())) {
            BlockPos targetPos = sourcePos.offset(offset);

            // 获取源方块状态
            BlockState sourceState = world.getBlockState(sourcePos);
            if (!world.isLoaded(targetPos)) {
                // 强制加载区块（会在当前线程加载，可能引起微小卡顿，但保证后续操作正常）
                world.getChunk(targetPos);
            }
            BlockState AIR = Blocks.AIR.defaultBlockState();
            if (!world.setBlock(targetPos, AIR, 0)) {
                // 是空气
                // continue;
            }

            if (sourceState.isAir()) {
                // 空气方块无需复制，但也可以选择复制（将目标置为空气）
                continue;
            }

            // 在目标位置设置方块状态，flags = 0 表示不进行任何更新（无邻居通知、无光照更新、无渲染更新）

            world.getChunk(targetPos).setBlockState(targetPos, sourceState, false);
            world.setBlock(targetPos, sourceState, 0);
            var tmp = sourceState.getOptionalValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
            if (!tmp.isEmpty()) {
                if (tmp.get().equals(DoubleBlockHalf.LOWER)) {
                    BlockState sourceUpperState = world.getBlockState(sourcePos.above());
                    world.setBlock(targetPos.above(), sourceUpperState, 0);

                }
            }
            // 复制方块实体（如果有）
            BlockEntity sourceEntity = world.getBlockEntity(sourcePos);
            if (sourceEntity != null) {
                BlockEntity targetEntity = world.getBlockEntity(targetPos);
                if (targetEntity != null && targetEntity.getType() == sourceEntity.getType()) {
                    // 将源方块实体的数据写入目标方块实体
                    CompoundTag nbt = sourceEntity.saveCustomOnly(world.registryAccess());
                    DataComponentMap components = sourceEntity.components();
                    targetEntity.setComponents(components);
                    targetEntity.loadCustomOnly(nbt, world.registryAccess());
                    targetEntity.setChanged(); // 标记需要保存
                }
            }
        }
    }
}