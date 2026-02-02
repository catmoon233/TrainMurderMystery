package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.TMMGameModes;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.item.KnifeItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public record KnifeStabPayload(int target) implements CustomPacketPayload {
    public static final Type<KnifeStabPayload> ID = new Type<>(TMM.id("knifestab"));
    public static final StreamCodec<FriendlyByteBuf, KnifeStabPayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT,
            KnifeStabPayload::target, KnifeStabPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<KnifeStabPayload> {
        @Override
        public void receive(@NotNull KnifeStabPayload payload, ServerPlayNetworking.@NotNull Context context) {
            ServerPlayer player = context.player();
            if (!(player.serverLevel().getEntity(payload.target()) instanceof ServerPlayer target))
                return;
            if (target.distanceTo(player) > 3.0)
                return;
            GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
            final var role = game.getRole(player);
            if (role != null) {
                if (!role.onUseKnifeHit(player, target)) {
                    return;
                }
            }
            GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.KNIFE);
            KnifeItem.PlayerKilledPlayer.accept(target, player);
            target.playSound(TMMSounds.ITEM_KNIFE_STAB, 1.0f, 1.0f);
            player.swing(InteractionHand.MAIN_HAND);
            if (!player.isCreative()
                    && GameWorldComponent.KEY.get(context.player().level()).getGameMode() != TMMGameModes.LOOSE_ENDS) {
                player.getCooldowns().addCooldown(TMMItems.KNIFE, GameConstants.ITEM_COOLDOWNS.get(TMMItems.KNIFE));
            }
        }
    }
}