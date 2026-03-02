package dev.doctor4t.trainmurdermystery.item;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.ScopeOverlayRenderer;
import dev.doctor4t.trainmurdermystery.client.particle.HandParticle;
import dev.doctor4t.trainmurdermystery.client.render.TMMRenderLayers;
import dev.doctor4t.trainmurdermystery.compat.CrosshairaddonsCompat;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMDataComponentTypes;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.GunShootPayload;
import dev.doctor4t.trainmurdermystery.util.SniperProjectileUtil;
import dev.doctor4t.trainmurdermystery.util.SniperShootPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SniperRifleItem extends Item {
    public static final String SCOPE_ATTACHED_KEY = "ScopeAttached";
    public static final String AMMO_COUNT_KEY = "AmmoCount";
    public static final int MAX_AMMO = 2;

    public SniperRifleItem(Properties settings) {
        super(settings.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);

        if (world.isClientSide) {
            final var gameComponent = TMMClient.gameComponent;
            if (gameComponent != null) {
                final var role = gameComponent.getRole(user);
                if (role != null) {
                    if (!role.onUseGun(user)) {
                        return InteractionResultHolder.fail(stack);
                    }
                }
            }

            // 检查是否蹲下
            boolean isSneaking = user.isShiftKeyDown();

            if (isSneaking) {
                // 蹲下右键：同普通右键（射击）
                if (hasScopeAttached(stack)) {
                    // 已安装倍镜，右键先切换到瞄准模式，再次右键射击
                    // 第一次右键：进入瞄准模式
                    if (!ScopeOverlayRenderer.isInScopeView()) {
                        ScopeOverlayRenderer.setInScopeView(true);
                        return InteractionResultHolder.success(stack);
                    }
                    // 已在瞄准模式，射击
                    int currentAmmo = getAmmoCount(stack);
                    if (currentAmmo <= 0) {
                        ScopeOverlayRenderer.setInScopeView(false);
                        return InteractionResultHolder.fail(stack); // 没有子弹
                    }
                    shoot(world, user, stack);
                    ScopeOverlayRenderer.setInScopeView(false);
                } else {
                    // 未安装倍镜，直接射击
                    int currentAmmo = getAmmoCount(stack);
                    if (currentAmmo <= 0) {
                        return InteractionResultHolder.fail(stack); // 没有子弹
                    }
                    shoot(world, user, stack);
                }
            } else {
                // 不蹲下：右键射击
                if (hasScopeAttached(stack)) {
                    // 已安装倍镜，右键先切换到瞄准模式，再次右键射击
                    // 第一次右键：进入瞄准模式
                    if (!ScopeOverlayRenderer.isInScopeView()) {
                        ScopeOverlayRenderer.setInScopeView(true);
                        return InteractionResultHolder.success(stack);
                    }
                    // 已在瞄准模式，射击
                    int currentAmmo = getAmmoCount(stack);
                    if (currentAmmo <= 0) {
                        ScopeOverlayRenderer.setInScopeView(false);
                        return InteractionResultHolder.fail(stack); // 没有子弹
                    }
                    shoot(world, user, stack);
                    ScopeOverlayRenderer.setInScopeView(false);
                } else {
                    // 未安装倍镜，直接射击
                    int currentAmmo = getAmmoCount(stack);
                    if (currentAmmo <= 0) {
                        return InteractionResultHolder.fail(stack); // 没有子弹
                    }
                    shoot(world, user, stack);
                }
            }
        } else {
            // 服务端逻辑
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(world);
            final var role = gameWorldComponent.getRole(user);
            if (role != null) {
                if (!role.onUseGun(user)) {
                    return InteractionResultHolder.fail(stack);
                }
            }
        }

        return InteractionResultHolder.consume(stack);
    }

    private void shoot(Level world, Player user, ItemStack stack) {
        if (world.isClientSide) {
            // 客户端射击逻辑
            HitResult collision = getGunTarget(user);
            if (collision instanceof EntityHitResult entityHitResult) {
                Entity target = entityHitResult.getEntity();
                ClientPlayNetworking.send(new SniperShootPayload(SniperShootPayload.Action.SHOOT, target.getId()));
                CrosshairaddonsCompat.arrowHit();
            } else {
                ClientPlayNetworking.send(new SniperShootPayload(SniperShootPayload.Action.SHOOT, -1));
            }
            user.setXRot(user.getXRot() - 4);
            spawnHandParticle();
        }
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

    public static HitResult getGunTarget(Player user) {
        return SniperProjectileUtil.getSniperHitResult(user,
                entity -> entity instanceof Player player && GameFunctions.isPlayerAliveAndSurvival(player), 150F);
    }

    // 倍镜相关方法
    public static boolean hasScopeAttached(ItemStack stack) {
        return stack.getOrDefault(TMMDataComponentTypes.SCOPE_ATTACHED, false);
    }

    public static void setScopeAttached(ItemStack stack, boolean attached) {
        stack.set(TMMDataComponentTypes.SCOPE_ATTACHED, attached);
    }

    // 子弹相关方法
    public static int getAmmoCount(ItemStack stack) {
        return stack.getOrDefault(TMMDataComponentTypes.AMMO_COUNT, 0);
    }

    public static void setAmmoCount(ItemStack stack, int count) {
        stack.set(TMMDataComponentTypes.AMMO_COUNT, Math.min(count, MAX_AMMO));
    }

    public static void consumeAmmo(ItemStack stack) {
        int currentAmmo = getAmmoCount(stack);
        setAmmoCount(stack, Math.max(0, currentAmmo - 1));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        int ammo = getAmmoCount(stack);
        boolean hasScope = hasScopeAttached(stack);
        tooltip.add(Component.translatable("item.trainmurdermystery.sniper_rifle.ammo", ammo, MAX_AMMO));
        tooltip.add(Component.translatable("item.trainmurdermystery.sniper_rifle.scope", hasScope ? Component.translatable("item.trainmurdermystery.sniper_rifle.scope.installed") : Component.translatable("item.trainmurdermystery.sniper_rifle.scope.not_installed")));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        int ammo = getAmmoCount(stack);
        return (int) ((double) ammo / MAX_AMMO * 13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return 0xFF5500;
    }
}
