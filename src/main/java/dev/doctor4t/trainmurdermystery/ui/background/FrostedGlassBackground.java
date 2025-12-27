package dev.doctor4t.trainmurdermystery.ui.background;

import com.daqem.uilib.client.gui.background.AbstractBackground;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 磨砂玻璃背景效果 - 带有透明度和景深模糊
 */
public class FrostedGlassBackground extends AbstractBackground<FrostedGlassBackground> {

    private final int borderColor;
    private final int fillColor;
    private final int borderWidth;
    private final int cornerRadius;

    /**
     * @param width 宽度
     * @param height 高度
     * @param fillColor 填充颜色 (ARGB格式，例如 0x88000000 为半透明黑色)
     * @param borderColor 边框颜色 (ARGB格式)
     * @param borderWidth 边框宽度
     * @param cornerRadius 圆角半径
     */
    public FrostedGlassBackground(int width, int height, int fillColor, int borderColor, int borderWidth, int cornerRadius) {
        super(width, height);
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.cornerRadius = cornerRadius;
    }

    public FrostedGlassBackground(int x, int y, int width, int height, int fillColor, int borderColor, int borderWidth, int cornerRadius) {
        super(x, y, width, height);
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.cornerRadius = cornerRadius;
    }

    @Override
    public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int width = getWidth();
        int height = getHeight();

        // 绘制主体半透明背景
        drawContext.fill(0, 0, width, height, fillColor);

        // 绘制渐变叠加层，增强景深效果
        int gradientTop = adjustAlpha(fillColor, 0.3f);
        int gradientBottom = adjustAlpha(fillColor, 0.15f);
        drawContext.fillGradient(0, 0, width, height / 2, gradientTop, gradientBottom);

        // 绘制边框
        if (borderWidth > 0) {
            // 顶边
            drawContext.fill(0, 0, width, borderWidth, borderColor);
            // 底边
            drawContext.fill(0, height - borderWidth, width, height, borderColor);
            // 左边
            drawContext.fill(0, borderWidth, borderWidth, height - borderWidth, borderColor);
            // 右边
            drawContext.fill(width - borderWidth, borderWidth, width, height - borderWidth, borderColor);
        }

        // 添加微妙的高光效果
        int highlightColor = adjustAlpha(0xFFFFFFFF, 0.1f);
        drawContext.fill(borderWidth, borderWidth, width - borderWidth, borderWidth + 1, highlightColor);

        RenderSystem.disableBlend();
    }

    /**
     * 调整颜色的透明度
     */
    private int adjustAlpha(int color, float alphaMultiplier) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        
        int newAlpha = (int) (alpha * alphaMultiplier);
        return (newAlpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
