package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record GunShootPayload(int target) implements CustomPacketPayload {
    public static final Type<GunShootPayload> ID = new Type<>(TMM.id("gunshoot"));
    public static final StreamCodec<FriendlyByteBuf, GunShootPayload> CODEC = StreamCodec.composite(ByteBufCodecs.INT, GunShootPayload::target, GunShootPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<GunShootPayload> {
        @Override
        public void receive(@NotNull GunShootPayload payload, ServerPlayNetworking.@NotNull Context context) {
            ServerPlayer player = context.player();
            ItemStack mainHandStack = player.getMainHandItem();
            if (!mainHandStack.is(TMMItemTags.GUNS)) return;
            if (player.getCooldowns().isOnCooldown(mainHandStack.getItem())) return;

            player.level().playSound(null, player.getX(), player.getEyeY(), player.getZ(), TMMSounds.ITEM_REVOLVER_CLICK, SoundSource.PLAYERS, 0.5f, 1f + player.getRandom().nextFloat() * .1f - .05f);

            // cancel if derringer has been shot
            Boolean isUsed = mainHandStack.get(TMMDataComponentTypes.USED);
            if (mainHandStack.is(TMMItems.DERRINGER)) {
                if (isUsed == null) {
                    isUsed = false;
                }

                if (isUsed) {
                    return;
                }

                if (!player.isCreative()) mainHandStack.set(TMMDataComponentTypes.USED, true);
            }

            if (player.serverLevel().getEntity(payload.target()) instanceof Player target && target.distanceTo(player) < 65.0) {
                GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
                Item revolver = TMMItems.REVOLVER;

                boolean backfire = false;

                if (game.isInnocent(target) && !player.isCreative() && mainHandStack.is(revolver)) {
                    // backfire: if you kill an innocent you have a chance of shooting yourself instead
                    if (game.isInnocent(player) && player.getRandom().nextFloat() <= game.getBackfireChance()) {
                        backfire = true;
                        GameFunctions.killPlayer(player, true, player, GameConstants.DeathReasons.GUN);
                    } else {
                        Scheduler.schedule(() -> {
                            if (!context.player().getInventory().contains((s) -> s.is(TMMItemTags.GUNS))) return;
                            player.getInventory().clearOrCountMatchingItems((s) -> s.is(revolver), 1, player.getInventory());
                            ItemEntity item = player.drop(revolver.getDefaultInstance(), false, false);
                            if (item != null) {
                                item.setPickUpDelay(10);
                                item.setThrower(player);
                            }
                            ServerPlayNetworking.send(player, new GunDropPayload());
                            PlayerMoodComponent.KEY.get(player).setMood(0);
                        }, 4);
                    }
                }

                if (!backfire) {
                    GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.GUN);
                }
            }

            player.level().playSound(null, player.getX(), player.getEyeY(), player.getZ(), TMMSounds.ITEM_REVOLVER_SHOOT, SoundSource.PLAYERS, 5f, 1f + player.getRandom().nextFloat() * .1f - .05f);

            for (ServerPlayer tracking : PlayerLookup.tracking(player))
                ServerPlayNetworking.send(tracking, new ShootMuzzleS2CPayload(player.getId()));
            ServerPlayNetworking.send(player, new ShootMuzzleS2CPayload(player.getId()));
            if (!player.isCreative())
                player.getCooldowns().addCooldown(mainHandStack.getItem(), GameConstants.ITEM_COOLDOWNS.getOrDefault(mainHandStack.getItem(), 0));
        }
    }
}