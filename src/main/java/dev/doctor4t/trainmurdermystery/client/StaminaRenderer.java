package dev.doctor4t.trainmurdermystery.client;

import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.PlayerStaminaGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.client.gui.GuiGraphics;

import org.jetbrains.annotations.NotNull;

public class StaminaRenderer {
	public static StaminaBarRenderer view = new StaminaBarRenderer();
	public static float offsetDelta = 0f;

	// 添加刀蓄满力的视觉效果相关变量
	private static boolean knifeFullyCharged = false;
	private static int flashTimer = 0;
	private static final int FLASH_DURATION = 15; // 闪光持续时间（ticks）

	// 添加屏幕边缘红色效果相关变量
	private static int screenRedEffectTimer = 0;
	private static final int SCREEN_RED_EFFECT_DURATION = 20; // 屏幕红色效果持续时间（ticks）
	private static final float MAX_RED_INTENSITY = 0.5f; // 最大红色强度（0-1）

	// 主手物品冷却相关变量
	private static float lastCooldown = 0f;
	private static boolean playedCooldownSound = false;
	private static ItemStack lastMainHandStack = ItemStack.EMPTY; // 用于跟踪上一次的主手物品

	public interface StaminaProvider {
		float getCurrentStamina(Player clientPlayerEntity);
		float getMaxStamina(Player clientPlayerEntity);
		float getStaminaPercentage(Player clientPlayerEntity); // 0.0到1.0之间的值
	}

	// 默认的体力提供者（临时使用）
	private static StaminaProvider staminaProvider = new StaminaProvider() {

		@Override
		public float getCurrentStamina(Player clientPlayerEntity) {
			if (!clientPlayerEntity.level().isClientSide
					|| !(clientPlayerEntity instanceof PlayerStaminaGetter provider))
				return 0;
			return provider.trainmurdermystery$getStamina();
		}

		@Override
		public float getMaxStamina(Player clientPlayerEntity) {
			GameWorldComponent gameComponent = GameWorldComponent.KEY.get(clientPlayerEntity.level());
			if (GameFunctions.isPlayerAliveAndSurvival(clientPlayerEntity) && gameComponent != null ) {
				Role role = gameComponent.getRole(clientPlayerEntity);
				if (role == null) {
					return 0;
				}
				return role.getMaxSprintTime();
			}
			return 0;
		}

		@Override
		public float getStaminaPercentage(Player clientPlayerEntity) {
			return Mth.clamp(getCurrentStamina(clientPlayerEntity) / getMaxStamina(clientPlayerEntity), 0f, 1f);
		}
	};

	public static void setStaminaProvider(StaminaProvider provider) {
		staminaProvider = provider;
	}

	public static void renderHud(@NotNull LocalPlayer player, @NotNull GuiGraphics context, float delta) {
		if (staminaProvider == null) return;

		float maxStamina = staminaProvider.getMaxStamina(player);
		float staminaPercent = staminaProvider.getStaminaPercentage(player);

		final var mainHandStack = player.getMainHandItem();
		boolean isChargingWeapon = false;
		if ( mainHandStack.getItem() == TMMItems.GRENADE){
			maxStamina = 20;
			final var itemUseTime = player.getTicksUsingItem();
			staminaPercent = Math.min( (float) itemUseTime / 20,1f);
			isChargingWeapon = true;
		}
		if (mainHandStack.getItem() == TMMItems.KNIFE ){
			maxStamina = 8;
			final var itemUseTime = player.getTicksUsingItem();
			staminaPercent = Math.min( (float) itemUseTime / 10,1f);
			isChargingWeapon = true;

			// 检测刀是否完全蓄力
			if (itemUseTime >= 10 && !knifeFullyCharged) {
				knifeFullyCharged = true;
				flashTimer = FLASH_DURATION; // 开始闪光效果
				screenRedEffectTimer = SCREEN_RED_EFFECT_DURATION; // 触发屏幕红色效果
			} else if (itemUseTime < 10) {
				knifeFullyCharged = false;
			}
		}

		if (maxStamina <= 0) return; // 无体力系统

		// 使用与TimeRenderer类似的颜色逻辑
		if (Math.abs(view.getTarget() - staminaPercent) > 0.1f) {
			offsetDelta = staminaPercent > view.getTarget() ? .6f : -.6f;
		}
		offsetDelta = Mth.lerp(delta / 16, offsetDelta, 0f);

		view.setTarget(staminaPercent);

		// 计算颜色 - 绿色满体力，红色低体力
		float r = Mth.lerp(1f - staminaPercent, 0.2f, 1f);
		float g = Mth.lerp(staminaPercent, 0.2f, 1f);
		float b = 0.2f;
		int colour = Mth.color(r, g, b) | 0xFF000000;

		// 渲染主手物品冷却提示
		renderMainHandCooldown(context, player, delta);

		// 渲染体力条 - 移动到物品栏上方
		context.pose().pushPose();
		context.pose().translate(context.guiWidth() / 2f, context.guiHeight() - 35, 0); // 在物品栏上方显示

		// 检查是否应该禁用平滑动画（特别是对于武器蓄力）
		if ((TMMConfig.disableStaminaBarSmoothing && isChargingWeapon) || isChargingWeapon) {
			// 如果是刀且完全蓄力，则添加特殊效果
			if (mainHandStack.getItem() == TMMItems.KNIFE && knifeFullyCharged && flashTimer > 0) {
				// 创建闪烁效果
				int flashColour = (flashTimer % 4 < 2) ? 0xFFFF0000 : 0xFFFFFFFF; // 红白交替闪烁
				view.renderWithoutSmoothing(context, flashColour, staminaPercent);
				flashTimer--; // 减少闪光计时器
			} else {
				view.renderWithoutSmoothing(context, colour, staminaPercent);
			}
		} else {
			view.render(context, colour, delta);
		}

		context.pose().popPose();

		// 渲染屏幕边缘红色效果
		renderScreenRedEffect(context, delta);
	}

	/**
	 * 渲染主手物品冷却提示
	 */
	private static void renderMainHandCooldown(@NotNull GuiGraphics context, @NotNull LocalPlayer player, float delta) {
		ItemStack mainHandStack = player.getMainHandItem();
		if (!mainHandStack.isEmpty()) {
			ItemCooldowns cooldowns = player.getCooldowns();
			float cooldown = cooldowns.getCooldownPercent(mainHandStack.getItem(), delta);

			// 检查是否是同一个物品且冷却刚刚结束
			if (lastCooldown > 0 && cooldown == 0 && !playedCooldownSound && ItemStack.isSameItemSameComponents(lastMainHandStack, mainHandStack)) {
				// 播放冷却结束音效
				Minecraft.getInstance().getSoundManager().play(
						SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f)
				);
				playedCooldownSound = true;
			} else if (cooldown > 0 || !ItemStack.isSameItemSameComponents(lastMainHandStack, mainHandStack)) {
				// 如果物品已切换，则重置冷却音效标志
				if (!ItemStack.isSameItemSameComponents(lastMainHandStack, mainHandStack)) {
					// 如果切换到刀，则播放切刀音效
					if (mainHandStack.getItem() == TMMItems.KNIFE && lastMainHandStack.getItem() != TMMItems.KNIFE) {
						Minecraft.getInstance().getSoundManager().play(
								SimpleSoundInstance.forUI(SoundEvents.PLAYER_ATTACK_SWEEP, 0.5f, 2.8f)
						);
					}
					playedCooldownSound = false;
				}
				// 如果物品仍在冷却中，重置音效标志
				if (cooldown > 0) {
					playedCooldownSound = false;
				}
			}

			// 更新上一次冷却值和物品
			lastCooldown = cooldown;
			lastMainHandStack = mainHandStack.copy();

			// 如果物品在冷却中，显示冷却百分比
			if (cooldown > 0) {
				int screenWidth = context.guiWidth();
				int screenHeight = context.guiHeight();

				// 在屏幕中心稍上方显示冷却文字
				int x = screenWidth / 2;
				int y = screenHeight - 48; // 物品栏上方

				String cooldownText = String.format("%d%%", (int)(cooldown * 100));

				// 根据冷却百分比改变颜色：红色->橙色->绿色
				int textColor;
				if (cooldown > 0.7f) {
					textColor = 0xFFFF0000; // 红色
				} else if (cooldown > 0.3f) {
					textColor = 0xFFFFA500; // 橙色
				} else {
					textColor = 0xFF00FF00; // 绿色
				}

				// 绘制文字背景（半透明黑色）
//				int textWidth = Minecraft.getInstance().font.width(cooldownText);
//				int padding = 4;
//				context.fill(
//						x - textWidth / 2 - padding,
//						y - padding,
//						x + textWidth / 2 + padding,
//						y + 9 + padding,
//						0x80000000
//				);

				// 绘制冷却文字
				context.drawCenteredString(
						Minecraft.getInstance().font,
						cooldownText,
						x,
						y,
						textColor
				);
			}
		}
	}

	/**
	 * 渲染屏幕边缘红色效果（刀蓄力完毕时）
	 */
	private static void renderScreenRedEffect(@NotNull GuiGraphics context, float delta) {
		if (screenRedEffectTimer > 0) {
			int screenWidth = context.guiWidth();
			int screenHeight = context.guiHeight();

			// 计算红色效果的强度（随时间递减）
			float progress = (float) screenRedEffectTimer / SCREEN_RED_EFFECT_DURATION;
			float intensity = MAX_RED_INTENSITY * progress;

			// 设置红色颜色（带透明度）
			int redColor = (int)(intensity * 255) << 24 | 0xFF0000; // ARGB格式

			// 保存当前的混合状态
			PoseStack poseStack = context.pose();
			poseStack.pushPose();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			// 渲染四个边缘的红色渐变
			int edgeWidth = (int)(screenWidth * 0.1f); // 边缘宽度为屏幕宽度的10%
			int edgeHeight = (int)(screenHeight * 0.1f); // 边缘高度为屏幕高度的10%

			// 顶部边缘（从上到下的渐变）
			for (int i = 0; i < edgeHeight; i++) {
				float alpha = (1f - (float)i / edgeHeight) * intensity;
				int color = (int)(alpha * 255) << 24 | 0xFF0000;
				context.fill(0, i, screenWidth, i + 1, color);
			}

			// 底部边缘（从下到上的渐变）
			for (int i = 0; i < edgeHeight; i++) {
				float alpha = (1f - (float)i / edgeHeight) * intensity;
				int color = (int)(alpha * 255) << 24 | 0xFF0000;
				context.fill(0, screenHeight - i - 1, screenWidth, screenHeight - i, color);
			}

			// 左侧边缘（从左到右的渐变）
			for (int i = 0; i < edgeWidth; i++) {
				float alpha = (1f - (float)i / edgeWidth) * intensity;
				int color = (int)(alpha * 255) << 24 | 0xFF0000;
				context.fill(i, edgeHeight, i + 1, screenHeight - edgeHeight, color);
			}

			// 右侧边缘（从右到左的渐变）
			for (int i = 0; i < edgeWidth; i++) {
				float alpha = (1f - (float)i / edgeWidth) * intensity;
				int color = (int)(alpha * 255) << 24 | 0xFF0000;
				context.fill(screenWidth - i - 1, edgeHeight, screenWidth - i, screenHeight - edgeHeight, color);
			}

			RenderSystem.disableBlend();
			poseStack.popPose();

			// 减少计时器
			screenRedEffectTimer--;
		}
	}

	public static void tick() {
		view.update();
		// 更新闪光计时器
		if (flashTimer > 0) {
			flashTimer--;
		}

		// 如果不在使用刀，重置蓄力状态
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null) {
			ItemStack mainHandStack = minecraft.player.getMainHandItem();
			if (mainHandStack.getItem() != TMMItems.KNIFE) {
				knifeFullyCharged = false;
				flashTimer = 0;
			}
		}
	}

	public static class StaminaBarRenderer {
		private float target;
		private float currentValue;
		private float lastValue;

		public void setTarget(float target) {
			this.target = Mth.clamp(target, 0f, 1f);
		}

		public void update() {
			this.lastValue = this.currentValue;
			this.currentValue = Mth.lerp(0.15f, this.currentValue, this.target);
			if (Math.abs(this.currentValue - this.target) < 0.01f) {
				this.currentValue = this.target;
			}
		}

		public void render(@NotNull GuiGraphics context, int colour, float delta) {
			float value = Mth.lerp(delta, this.lastValue, this.currentValue);

			// 体力条参数 - 更现代、更扁平的设计
			int barWidth = 120; // 总宽度增加
			int barHeight = 2;  // 高度减小变得更扁平
			int halfWidth = barWidth / 2;

			// 绘制背景（更现代化的半透明黑色）
			int backgroundColor = 0x66000000; // 更透明的背景
			context.fill(-halfWidth, -barHeight/2, halfWidth, barHeight/2, backgroundColor);

			// 计算当前体力条宽度
			int currentWidth = Math.round(barWidth * value);
			int currentHalfWidth = currentWidth / 2;

			if (currentWidth > 0) {
				// 绘制体力条（从中间向两边延伸）
				context.fill(-currentHalfWidth, -barHeight/2, currentHalfWidth, barHeight/2, colour);
			}

			// 绘制中心分隔线（更窄）
			int centerLineColor = 0x80FFFFFF;
			context.fill(-1, -barHeight/2 + 1, 1, barHeight/2 - 1, centerLineColor); // 更窄的线条
		}

		public void renderWithoutSmoothing(@NotNull GuiGraphics context, int colour, float value) {
			// 体力条参数 - 更现代、更扁平的设计
			int barWidth = 120; // 总宽度增加
			int barHeight = 2;  // 高度减小变得更扁平
			int halfWidth = barWidth / 2;

			// 绘制背景（更现代化的半透明黑色）
			int backgroundColor = 0x66000000; // 更透明的背景
			context.fill(-halfWidth, -barHeight/2, halfWidth, barHeight/2, backgroundColor);

			// 计算当前体力条宽度
			int currentWidth = Math.round(barWidth * value);
			int currentHalfWidth = currentWidth / 2;

			if (currentWidth > 0) {
				// 绘制体力条（从中间向两边延伸）
				context.fill(-currentHalfWidth, -barHeight/2, currentHalfWidth, barHeight/2, colour);
			}

			// 绘制中心分隔线（更窄）
			int centerLineColor = 0x80FFFFFF;
			context.fill(-1, -barHeight/2 + 1, 1, barHeight/2 - 1, centerLineColor); // 更窄的线条
		}

		public float getTarget() {
			return this.target;
		}
	}
}