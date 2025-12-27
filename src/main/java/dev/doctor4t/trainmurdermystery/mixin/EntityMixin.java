package dev.doctor4t.trainmurdermystery.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private Level level;

    @WrapMethod(method = "canCollideWith")
    protected boolean tmm$solid(Entity other, Operation<Boolean> original) {
        if (GameWorldComponent.KEY.get(this.level).isRunning()) {
            Entity self = (Entity) (Object) this;
            if (self instanceof Player && other instanceof Player) return true;
        }
        return original.call(other);
    }
}