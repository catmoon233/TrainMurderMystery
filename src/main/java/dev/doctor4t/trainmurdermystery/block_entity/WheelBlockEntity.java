package dev.doctor4t.trainmurdermystery.block_entity;

import dev.doctor4t.trainmurdermystery.block.WheelBlock;
import dev.doctor4t.trainmurdermystery.index.TMMBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class WheelBlockEntity extends BlockEntity {
    public WheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static WheelBlockEntity create(BlockPos pos, BlockState state) {
        return new WheelBlockEntity(TMMBlockEntities.WHEEL, pos, state);
    }

    public float getYaw() {
        return 180 - this.getFacing().asRotation();
    }

    public Direction getFacing() {
        return this.getCachedState().get(WheelBlock.FACING);
    }
}
