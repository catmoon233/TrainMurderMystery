package dev.doctor4t.trainmurdermystery.item;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.particle.HandParticle;
import dev.doctor4t.trainmurdermystery.client.render.TMMRenderLayers;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.util.GunShootPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class RevolverItem extends Item {
    public RevolverItem(Settings settings) {
        super(settings.maxDamage(4)); // 设置最大耐久度为4
    }


    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // 检查物品是否已经损坏（耐久度为0）
//        if (stack.getDamage() >= stack.getMaxDamage()-1) {
//            return TypedActionResult.fail(stack);
//        }
        
        if (world.isClient) {
            HitResult collision = getGunTarget(user);
            if (collision instanceof EntityHitResult entityHitResult) {
                Entity target = entityHitResult.getEntity();
                ClientPlayNetworking.send(new GunShootPayload(target.getId()));
            } else {
                ClientPlayNetworking.send(new GunShootPayload(-1));
            }
            user.setPitch(user.getPitch() - 4);
            spawnHandParticle();
        } else {
            // 在服务端消耗耐久度
//            stack.setDamage(stack.getDamage() + 1);
        }
        return TypedActionResult.consume(stack);
    }

    public static void spawnHandParticle() {
        HandParticle handParticle = new HandParticle()
                .setTexture(TMM.id("textures/particle/gunshot.png"))
                .setPos(0.1f, 0.275f, -0.2f)
                .setMaxAge(3)
                .setSize(0.5f)
                .setVelocity(0f, 0f, 0f)
                .setLight(15, 15)
                .setAlpha(1f, 0.1f)
                .setRenderLayer(TMMRenderLayers::additive);
        TMMClient.handParticleManager.spawn(handParticle);
    }

    public static HitResult getGunTarget(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, entity -> entity instanceof PlayerEntity player && GameFunctions.isPlayerAliveAndSurvival(player), 15f);
    }
}