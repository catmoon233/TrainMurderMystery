package dev.doctor4t.trainmurdermystery.block;

import dev.doctor4t.trainmurdermystery.block.entity.SeatEntity;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MountableBlock extends Block {

    public MountableBlock(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return super.getShape(state, world, pos, context);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        float radius = 1;
        if (!player.isShiftKeyDown()
                && player.position().subtract(pos.getCenter()).length() <= 1.5f
                && !(player.getMainHandItem().getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof MountableBlock)
                && world.getEntitiesOfClass(SeatEntity.class, AABB.ofSize(pos.getCenter(), radius, radius, radius), Entity::isAlive).isEmpty()) {

            if (world.isClientSide) {
                return InteractionResult.sidedSuccess(true);
            }

            SeatEntity seatEntity = TMMEntities.SEAT.create(world);

            if (seatEntity == null) {
                return InteractionResult.PASS;
            }

            Vec3 sitPos = this.getSitPos(world, state, pos);
            Vec3 vec3d = Vec3.atLowerCornerOf(pos).add(sitPos);
            seatEntity.moveTo(vec3d.x, vec3d.y, vec3d.z, 0, 0);
            seatEntity.setSeatPos(pos);
            world.addFreshEntity(seatEntity);
            player.startRiding(seatEntity);

            return InteractionResult.sidedSuccess(false);
        } else {
            return InteractionResult.PASS;
        }
    }

    public abstract Vec3 getSitPos(Level world, BlockState state, BlockPos pos);
}
