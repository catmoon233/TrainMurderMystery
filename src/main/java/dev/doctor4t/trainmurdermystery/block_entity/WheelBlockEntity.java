package dev.doctor4t.trainmurdermystery.block_entity;

import dev.doctor4t.trainmurdermystery.block.WheelBlock;
import dev.doctor4t.trainmurdermystery.index.TMMBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class WheelBlockEntity extends BlockEntity {
    public WheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static WheelBlockEntity create(BlockPos pos, BlockState state) {
        return new WheelBlockEntity(TMMBlockEntities.WHEEL, pos, state);
    }

    public float getYaw() {
        return 180 - this.getFacing().toYRot();
    }

    public Direction getFacing() {
        return this.getBlockState().getValue(WheelBlock.FACING);
    }
}
