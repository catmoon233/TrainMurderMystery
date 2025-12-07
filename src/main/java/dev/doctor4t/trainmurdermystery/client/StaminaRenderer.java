package dev.doctor4t.trainmurdermystery.client;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.util.PlayerStaminaGetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class StaminaRenderer {
	public static StaminaBarRenderer view = new StaminaBarRenderer();
	public static float offsetDelta = 0f;


	// 体力提供者接口 - 留给你实现
	public interface StaminaProvider {
		float getCurrentStamina(PlayerEntity clientPlayerEntity);
		float getMaxStamina(PlayerEntity clientPlayerEntity);
		float getStaminaPercentage(PlayerEntity clientPlayerEntity); // 0.0到1.0之间的值
	}


	// 默认的体力提供者（临时使用）
	private static StaminaProvider staminaProvider = new StaminaProvider() {

		@Override
		public float getCurrentStamina(PlayerEntity clientPlayerEntity) {
			if (!clientPlayerEntity.getWorld().isClient
					|| !(clientPlayerEntity instanceof PlayerStaminaGetter provider))
				return 0;
			return provider.trainmurdermystery$getStamina();
		}

		@Override
		public float getMaxStamina(PlayerEntity clientPlayerEntity) {
			GameWorldComponent gameComponent = GameWorldComponent.KEY.get(clientPlayerEntity.getWorld());
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
		public float getStaminaPercentage(PlayerEntity clientPlayerEntity) {
			return MathHelper.clamp(getCurrentStamina(clientPlayerEntity) / getMaxStamina(clientPlayerEntity), 0f, 1f);
		}
	};

	public static void setStaminaProvider(StaminaProvider provider) {
		staminaProvider = provider;
	}

	public static void renderHud(@NotNull ClientPlayerEntity player, @NotNull DrawContext context, float delta) {
		if (staminaProvider == null) return;

		//float currentStamina = staminaProvider.getCurrentStamina();
		float maxStamina = staminaProvider.getMaxStamina(player);
		float staminaPercent = staminaProvider.getStaminaPercentage(player);

		if (maxStamina <= 0) return; // 无体力系统

		// 使用与TimeRenderer类似的颜色逻辑
		if (Math.abs(view.getTarget() - staminaPercent) > 0.1f) {
			offsetDelta = staminaPercent > view.getTarget() ? .6f : -.6f;
		}
		offsetDelta = MathHelper.lerp(delta / 16, offsetDelta, 0f);

		view.setTarget(staminaPercent);

		// 计算颜色 - 绿色满体力，红色低体力
		float r = MathHelper.lerp(1f - staminaPercent, 0.2f, 1f);
		float g = MathHelper.lerp(staminaPercent, 0.2f, 1f);
		float b = 0.2f;
		int colour = MathHelper.packRgb(r, g, b) | 0xFF000000;

		// 渲染体力条 - 移动到物品栏上方
		context.getMatrices().push();
		context.getMatrices().translate(context.getScaledWindowWidth() / 2f, context.getScaledWindowHeight() - 35, 0); // 在物品栏上方显示
		view.render(context, colour, delta);
		context.getMatrices().pop();

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
			this.target = MathHelper.clamp(target, 0f, 1f);
		}

		public void update() {
			this.lastValue = this.currentValue;
			this.currentValue = MathHelper.lerp(0.15f, this.currentValue, this.target);
			if (Math.abs(this.currentValue - this.target) < 0.01f) {
				this.currentValue = this.target;
			}
		}

		public void render(@NotNull DrawContext context, int colour, float delta) {
			float value = MathHelper.lerp(delta, this.lastValue, this.currentValue);

			// 体力条参数 - 更现代、更扁平的设计
			int barWidth = 120; // 总宽度增加
			int barHeight = 1;  // 高度减小变得更扁平
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