package dev.doctor4t.trainmurdermystery.util;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerNunchuckComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.item.NunchuckItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record NunchuckHitPayload(int targetId, int direction) implements CustomPacketPayload {
    public static final Type<NunchuckHitPayload> ID = new Type<>(TMM.id("nunchuck_hit"));
    public static final StreamCodec<FriendlyByteBuf, NunchuckHitPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            NunchuckHitPayload::targetId,
            ByteBufCodecs.INT,
            NunchuckHitPayload::direction,
            NunchuckHitPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<NunchuckHitPayload> {
        @Override
        public void receive(@NotNull NunchuckHitPayload payload, ServerPlayNetworking.@NotNull Context context) {
            ServerPlayer attacker = context.player();

            // 检查冷却
            if (attacker.getCooldowns().isOnCooldown(TMMItems.NUNCHUCK)) {
                return;
            }

            // 获取主手物品
            if (!attacker.getMainHandItem().is(TMMItems.NUNCHUCK)) {
                return;
            }

            // 获取目标
            Player target = (Player) attacker.serverLevel().getEntity(payload.targetId());
            if (target == null || target == attacker) {
                return;
            }

            // 检查距离
            double distance = attacker.distanceTo(target);
            if (distance > 7.0) {
                return;
            }

            // 检查游戏状态
            GameWorldComponent game = GameWorldComponent.KEY.get(attacker.level());
            final var role = game.getRole(attacker);
            if (role != null && !role.onGunHit(attacker, target)) {
                return;
            }

            // 检查目标是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(target)) {
                return;
            }

            // 播放声音
            attacker.level().playSound(null, attacker.getX(), attacker.getEyeY(), attacker.getZ(),
                    TMMSounds.ITEM_REVOLVER_CLICK, SoundSource.PLAYERS, 1.0f, 1.0f);

            // 执行击退
            Vec3 knockbackDir = NunchuckItem.getKnockbackDirection(attacker, payload.direction());
            double knockbackStrength = 1.0;
            target.setDeltaMovement(knockbackDir.scale(knockbackStrength));
            target.hurtMarked = true;

            // 造成1点伤害
            target.hurt(attacker.level().damageSources().playerAttack(attacker), 1.0f);

            // 检查目标是否在方块侧面
            boolean targetNearBlock = NunchuckItem.isPlayerNearBlock(target);

            // 获取目标的击打记录组件
            PlayerNunchuckComponent targetComponent = PlayerNunchuckComponent.KEY.get(target);
            PlayerNunchuckComponent.HitRecord record = targetComponent.getHitRecord(attacker.getUUID());

            boolean shouldKill = false;

            if (record != null) {
                // 检查是否连续被击打3次
                if (record.hitCount >= 3) {
                    shouldKill = true;
                } else {
                    // 检查: 如果之前在方块侧面被击打，现在再次被击打则直接死亡
                    if (record.nearBlock) {
                        shouldKill = true;
                    } else {
                        // 增加击打次数
                        targetComponent.incrementHitCount(attacker.getUUID());
                        // 更新是否在方块侧面的状态
                        record.nearBlock = targetNearBlock;
                    }
                }
            } else {
                // 第一次被击打
                targetComponent.recordHit(attacker.getUUID(), payload.direction(), targetNearBlock);
            }

            // 如果应该击杀，执行击杀
            if (shouldKill) {
                GameFunctions.killPlayer(target, true, attacker, GameConstants.DeathReasons.NUNCHUCK);
                targetComponent.clearHitRecord(attacker.getUUID());

                // 设置物品冷却
                if (!attacker.isCreative()) {
                    attacker.getCooldowns().addCooldown(TMMItems.NUNCHUCK, 3 * 20); // 3秒
                }
            } else {
                // 不击杀的情况下，设置较短的冷却
                if (!attacker.isCreative()) {
                    attacker.getCooldowns().addCooldown(TMMItems.NUNCHUCK, 20); // 1秒
                }
            }
        }
    }
}
