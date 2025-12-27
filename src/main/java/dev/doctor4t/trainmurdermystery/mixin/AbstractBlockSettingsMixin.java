package dev.doctor4t.trainmurdermystery.mixin;

import dev.doctor4t.trainmurdermystery.util.BlockSettingsAdditions;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockBehaviour.Properties.class)
public class AbstractBlockSettingsMixin implements BlockSettingsAdditions {
    @Shadow
    boolean hasCollision;

    @Override
    public BlockBehaviour.Properties tmm$setCollidable(boolean collidable) {
        this.hasCollision = collidable;
        return (BlockBehaviour.Properties) (Object) this;
    }
}