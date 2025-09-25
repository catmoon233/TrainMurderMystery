package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.cca.TMMComponents;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundCategory;
import org.jetbrains.annotations.NotNull;

public record GunShootPayload(int target) implements CustomPayload {
	public static final Id<GunShootPayload> ID = new Id<>(TMM.id("gunshoot"));
	public static final PacketCodec<PacketByteBuf, GunShootPayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, GunShootPayload::target, GunShootPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<GunShootPayload> {
		@Override
		public void receive(@NotNull GunShootPayload payload, ServerPlayNetworking.@NotNull Context context) {
			var player = context.player();
			if (!player.getMainHandStack().isOf(TMMItems.REVOLVER)) return;
			if (player.getServerWorld().getEntityById(payload.target()) instanceof PlayerEntity target && target.distanceTo(player) < 65.0) {
				var game = TMMComponents.GAME.get(player.getWorld());
				if (game.isCivilian(target) && game.isCivilian(player) && !player.isCreative()) {
					PlayerMoodComponent.KEY.get(player).setMood(0);
					Scheduler.schedule(() -> {
						player.getInventory().remove((s) -> s.isOf(TMMItems.REVOLVER), 1, player.getInventory());
						player.dropItem(TMMItems.REVOLVER.getDefaultStack(), false, false);
						ServerPlayNetworking.send(player, new GunDropPayload());
						}, 4
					);
				}
				GameFunctions.killPlayer(target, true);
			}
			player.getWorld().playSound(null, player.getX(), player.getEyeY(), player.getZ(), TMMSounds.ITEM_REVOLVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1f + player.getRandom().nextFloat() * .1f - .05f);
			for (var tracking : PlayerLookup.tracking(player)) ServerPlayNetworking.send(tracking, new ShootMuzzleS2CPayload(player.getUuidAsString()));
			ServerPlayNetworking.send(player, new ShootMuzzleS2CPayload(player.getUuidAsString()));
			player.getWorld().playSound(null, player.getX(), player.getEyeY(), player.getZ(), TMMSounds.ITEM_REVOLVER_SHOOT, SoundCategory.PLAYERS, 5f, 1f + player.getRandom().nextFloat() * .1f - .05f);
			if (!player.isCreative()) player.getItemCooldownManager().set(TMMItems.REVOLVER, GameConstants.REVOLVER_COOLDOWN);
		}
	}
}