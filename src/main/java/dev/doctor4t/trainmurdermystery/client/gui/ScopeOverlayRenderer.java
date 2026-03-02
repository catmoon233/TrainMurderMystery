package dev.doctor4t.trainmurdermystery.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ScopeOverlayRenderer {
    private static boolean inScopeView = false;

    public static boolean isInScopeView() {
        return inScopeView;
    }

    public static void setInScopeView(boolean inScopeView) {
        ScopeOverlayRenderer.inScopeView = inScopeView;
    }

    public static void renderScopeOverlay(GuiGraphics context, DeltaTracker tickCounter) {
        if (!inScopeView) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int scopeRadius = Math.min(screenWidth, screenHeight) / 2;

        context.pose().pushPose();

        // 启用混合和深度测试
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 渲染边缘黑色遮蔽（四个角落，中间留空）
        int margin = 50; // 倍镜圆圈半径外的黑色遮蔽宽度
        int viewRadius = Math.min(screenWidth, screenHeight) / 3; // 可视区域半径
        
        // 上边缘
        context.fill(0, 0, screenWidth, centerY - viewRadius, 0xFF000000);
        // 下边缘
        context.fill(0, centerY + viewRadius, screenWidth, screenHeight, 0xFF000000);
        // 左边缘
        context.fill(0, centerY - viewRadius, centerX - viewRadius, centerY + viewRadius, 0xFF000000);
        // 右边缘
        context.fill(centerX + viewRadius, centerY - viewRadius, screenWidth, centerY + viewRadius, 0xFF000000);

        // 渲染倍镜准星（十字线）
        int crosshairThickness = 2;
        int crosshairLength = 15;

        // 水平线
        context.fill(centerX - crosshairLength, centerY - crosshairThickness / 2,
                   centerX + crosshairLength, centerY + crosshairThickness / 2, 0xFFFFFFFF);
        
        // 垂直线
        context.fill(centerX - crosshairThickness / 2, centerY - crosshairLength,
                   centerX + crosshairThickness / 2, centerY + crosshairLength, 0xFFFFFFFF);

        // 渲染倍镜圆圈（准星圈）
        int circleRadius = viewRadius;
        int circleThickness = 2;
        for (int angle = 0; angle < 360; angle += 1) {
            double rad = Math.toRadians(angle);
            int x1 = centerX + (int) (Math.cos(rad) * (circleRadius - circleThickness / 2));
            int y1 = centerY + (int) (Math.sin(rad) * (circleRadius - circleThickness / 2));
            int x2 = centerX + (int) (Math.cos(rad) * (circleRadius + circleThickness / 2));
            int y2 = centerY + (int) (Math.sin(rad) * (circleRadius + circleThickness / 2));
            context.fill(x1, y1, x2 + 1, y2 + 1, 0xFFDDDDDD);
        }

        // 渲染刻度线（垂直方向）
        int tickLength = 8;
        int tickThickness = 1;
        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue; // 跳过中心
            int tickY = centerY + i * 12;
            context.fill(centerX - tickLength / 2, tickY - tickThickness / 2,
                       centerX + tickLength / 2, tickY + tickThickness / 2, 0xFFAAAAAA);
        }

        // 渲染刻度线（水平方向）
        for (int i = -3; i <= 3; i++) {
            if (i == 0) continue; // 跳过中心
            int tickX = centerX + i * 12;
            context.fill(tickX - tickThickness / 2, centerY - tickLength / 2,
                       tickX + tickThickness / 2, centerY + tickLength / 2, 0xFFAAAAAA);
        }

        context.pose().popPose();
        RenderSystem.disableBlend();
    }
}
