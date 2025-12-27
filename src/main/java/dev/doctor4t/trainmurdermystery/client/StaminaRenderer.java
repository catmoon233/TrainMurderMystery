package dev.doctor4t.trainmurdermystery.client;

import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.PlayerStaminaGetter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class StaminaRenderer {
	public static StaminaBarRenderer view = new StaminaBarRenderer();
	public static float offsetDelta = 0f;


	// 体力提供者接口 - 留给你实现
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

		//float currentStamina = staminaProvider.getCurrentStamina();
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
			maxStamina = 10;
			final var itemUseTime = player.getTicksUsingItem();
			staminaPercent = Math.min( (float) itemUseTime / 10,1f);
			isChargingWeapon = true;
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

		// 渲染体力条 - 移动到物品栏上方
		context.pose().pushPose();
		context.pose().translate(context.guiWidth() / 2f, context.guiHeight() - 35, 0); // 在物品栏上方显示
		
		// 检查是否应该禁用平滑动画（特别是对于武器蓄力）
		if ((TMMConfig.disableStaminaBarSmoothing && isChargingWeapon) || isChargingWeapon) {
			view.renderWithoutSmoothing(context, colour, staminaPercent);
		} else {
			view.render(context, colour, delta);
		}
		
		context.pose().popPose();

		// 可选：显示体力数值

//        if (MinecraftClient.getInstance().debugRenderer) {
//            String staminaText = String.format("%.1f/%.1f", currentStamina, maxStamina);
//            int textX = context.getScaledWindowWidth() / 2 - 20;
//            int textY = 35;
//            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, staminaText, textX, textY, colour);
//        }
	}

	public static void tick() {
		view.update();
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