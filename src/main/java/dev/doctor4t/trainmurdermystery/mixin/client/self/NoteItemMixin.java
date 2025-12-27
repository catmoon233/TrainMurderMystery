package dev.doctor4t.trainmurdermystery.mixin.client.self;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.NoteScreen;
import dev.doctor4t.trainmurdermystery.item.NoteItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(NoteItem.class)
public class NoteItemMixin {

    @Inject(method = "use", at = @At("HEAD"))
    private void useClient(@NotNull Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!user.isShiftKeyDown()) {
            return;
        }
        Minecraft.getInstance().setScreen(new NoteScreen());
    }
}
