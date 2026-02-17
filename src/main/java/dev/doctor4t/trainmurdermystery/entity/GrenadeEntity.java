package dev.doctor4t.trainmurdermystery.entity;

import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMParticles;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrenadeEntity extends ThrowableItemProjectile {
    public GrenadeEntity(EntityType<?> ignored, Level world) {
        super(TMMEntities.GRENADE, world);
    }

    @Override
    protected Item getDefaultItem() {
        return TMMItems.THROWN_GRENADE;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel world) {
            // Consider sending this in one payload to reduce packets sent - SkyNotTheLimit
            world.playSound(null, this.blockPosition(), TMMSounds.ITEM_GRENADE_EXPLODE, SoundSource.PLAYERS, 5f, 1f + this.getRandom().nextFloat() * .1f - .05f);
            world.sendParticles(TMMParticles.BIG_EXPLOSION, this.getX(), this.getY() + .1f, this.getZ(), 1, 0, 0, 0, 0);
            world.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + .1f, this.getZ(), 100, 0, 0, 0, .2f);
            world.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, this.getDefaultItem().getDefaultInstance()), this.getX(), this.getY() + .1f, this.getZ(), 100, 0, 0, 0, 1f);

            for (ServerPlayer player : world.getPlayers(serverPlayerEntity ->
                    this.getBoundingBox().inflate(4.5f).contains(serverPlayerEntity.position()) &&
                            GameFunctions.isPlayerAliveAndSurvival(serverPlayerEntity))) {

                // 检查玩家与爆炸点之间是否有视线（无障碍物）
                if (hasLineOfSight(world, this.position().add(0,0.5,0), player)) {
                    GameFunctions.killPlayer(player, true, this.getOwner() instanceof Player playerEntity ? playerEntity : null, GameConstants.DeathReasons.GRENADE);
                }
            }

            this.discard();
        }
    }

    /**
     * 检查两点之间是否有视线（无障碍物阻挡）
     * @param world 世界
     * @param startPos 起点位置（爆炸点）
     * @param target 目标实体
     * @return 是否有直接视线
     */
    private boolean hasLineOfSight(ServerLevel world, Vec3 startPos, ServerPlayer target) {
        // 计算目标的有效位置（使用眼睛位置，因为玩家有高度）
        Vec3 targetPos = target.getEyePosition();

        // 创建一个ClipContext来检测视线
        ClipContext clipContext = new ClipContext(
                startPos,
                targetPos,
                ClipContext.Block.COLLIDER,  // 只检测有碰撞箱的方块
                ClipContext.Fluid.NONE,      // 忽略流体
                target
        );

        // 进行视线检测
        BlockHitResult blockHit = world.clip(clipContext);

        // 如果没有命中任何方块，或者命中的方块距离大于目标距离，说明视线畅通
        // 我们检查命中类型是否为MISS，或者命中位置到起点的距离是否大于等于起点到目标的距离
        return blockHit.getType() == HitResult.Type.MISS ||
                blockHit.getLocation().distanceTo(startPos) >= startPos.distanceTo(targetPos);
    }
}