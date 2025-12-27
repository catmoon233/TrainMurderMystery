package dev.doctor4t.trainmurdermystery.mixin;

import de.maxhenkel.voicechat.api.ServerPlayer;
import net.mehvahdjukaar.vista.common.TVBlock;
import net.mehvahdjukaar.vista.common.TVBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TVBlock.class)
public class CemaraMixin {
//    @Redirect(method = "onUseWithItem", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/vista/common/TVBlockEntity;interactWithPlayerItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/util/ItemActionResult;"))
//    public ItemActionResult onUseWithItem(TVBlockEntity instance, PlayerEntity playerEntity, Hand hand, ItemStack itemStack) {
//        playerEntity.getWorld().getBlockEntity(instance.)
//        if (playerEntity instanceof ServerPlayer sp && !this.isOtherPlayerEditing(pos, player)) {
//            // open gui (edit sign with empty hand)
//            this.setPlayerWhoMayEdit(player.getUUID());
//            NetworkHelper.sendToClientPlayer(sp, new ClientBoundControlViewFinderPacket(TileOrEntityTarget.of(this)));
//        }
//        return ItemActionResult.success(true);
//    }
}
