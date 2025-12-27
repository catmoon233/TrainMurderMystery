package dev.doctor4t.trainmurdermystery.item;

import dev.doctor4t.trainmurdermystery.entity.FirecrackerEntity;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.util.AdventureUsable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class FirecrackerItem extends Item implements AdventureUsable {
    public FirecrackerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(@NotNull UseOnContext context) {
        if (context.getClickedFace().equals(Direction.UP)) {
            Player player = context.getPlayer();
            Level world = player.level();
            if (!world.isClientSide) {
                FirecrackerEntity firecracker = TMMEntities.FIRECRACKER.create(world);
                Vec3 spawnPos = context.getClickLocation();

                firecracker.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());
                firecracker.setYRot(player.getYHeadRot());
                world.addFreshEntity(firecracker);
                if (!player.isCreative()) player.getItemInHand(context.getHand()).shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}