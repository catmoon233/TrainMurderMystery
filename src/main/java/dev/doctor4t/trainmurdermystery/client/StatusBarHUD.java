package dev.doctor4t.trainmurdermystery.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.minecraft.client.renderer.GameRenderer.getPositionShader;

public class StatusBarHUD {
    private static final StatusBarHUD INSTANCE = new StatusBarHUD();
    private final Map<String, StatusBar> statusBars = new ConcurrentHashMap<>();
    private final Map<String, Long> removalTimers = new ConcurrentHashMap<>();
    
    // 配置参数
    private static final float BAR_HEIGHT = 12.0f;
    private static final float BAR_WIDTH = 182.0f;
    private static final float BAR_SPACING = 5.0f;
    private static final long FADE_DURATION = 1000L; // 淡出时间
    private static final long DEFAULT_DURATION = 5000L; // 默认显示时间
    
    // 颜色配置
    private static final int BACKGROUND_COLOR = 0x66000000;
    private static final int BORDER_COLOR = 0xFF6C63FF;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_COLOR = 0xAA000000;
    
    // 预定义进度条颜色（可以根据进度值动态计算）
    private static final int[] PROGRESS_COLORS = {
            0xFF4CAF50, // 绿色 (高)
            0xFF8BC34A, 
            0xFFCDDC39, // 黄色
            0xFFFFC107,
            0xFFFF9800, // 橙色
            0xFFF44336  // 红色 (低)
    };

    private StatusBarHUD() {}

    public static StatusBarHUD getInstance() {
        return INSTANCE;
    }

    /**
     * 添加或更新状态条
     * @param id 唯一标识符
     * @param name 显示名称
     * @param currentProgress 当前进度
     * @param maxProgress 最大进度
     */
    public void addStatusBar(String id, String name, float currentProgress, float maxProgress) {
        addStatusBar(id, name, currentProgress, maxProgress, DEFAULT_DURATION);
    }

    /**
     * 添加或更新状态条（带持续时间）
     * @param id 唯一标识符
     * @param name 显示名称
     * @param currentProgress 当前进度
     * @param maxProgress 最大进度
     * @param durationMs 显示持续时间（毫秒）
     */
    public void addStatusBar(String id, String name, float currentProgress, float maxProgress, long durationMs) {
        StatusBar bar = statusBars.computeIfAbsent(id, k -> new StatusBar());
        bar.name = name;
        bar.currentProgress = Mth.clamp(currentProgress, 0, maxProgress);
        bar.maxProgress = maxProgress;
        bar.lastUpdateTime = System.currentTimeMillis();
        bar.duration = durationMs;
        
        // 重置移除计时器
        removalTimers.put(id, System.currentTimeMillis() + durationMs);
    }

    /**
     * 移除状态条
     * @param id 状态条ID
     */
    public void removeStatusBar(String id) {
        statusBars.remove(id);
        removalTimers.remove(id);
    }

    /**
     * 清除所有状态条
     */
    public void clearAllStatusBars() {
        statusBars.clear();
        removalTimers.clear();
    }

    /**
     * 渲染所有状态条
     */
    public void render(GuiGraphics guiGraphics, float partialTicks) {
        if (statusBars.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // 计算渲染位置（屏幕上方1/4处）
        int startY = screenHeight / 4;
        int currentY = startY;

        // 清理过期的状态条
        cleanupExpiredBars();

        // 获取排序后的状态条列表（按添加时间排序）
        List<Map.Entry<String, StatusBar>> sortedBars = new ArrayList<>(statusBars.entrySet());
        sortedBars.sort(Comparator.comparingLong(entry -> entry.getValue().lastUpdateTime));

        // 渲染每个状态条
        for (Map.Entry<String, StatusBar> entry : sortedBars) {
            StatusBar bar = entry.getValue();
            
            // 计算透明度（淡入淡出效果）
            long timeSinceUpdate = System.currentTimeMillis() - bar.lastUpdateTime;
            float alpha = 1.0f;
            
            // 淡入效果（前500ms）
            if (timeSinceUpdate < 500) {
                alpha = timeSinceUpdate / 500.0f;
            }
            // 淡出效果（最后1秒）
            else if (timeSinceUpdate > bar.duration - FADE_DURATION) {
                alpha = 1.0f - (timeSinceUpdate - (bar.duration - FADE_DURATION)) / (float) FADE_DURATION;
            }
            
            alpha = Mth.clamp(alpha, 0.0f, 1.0f);
            
            if (alpha <= 0.01f) continue;

            renderStatusBar(guiGraphics, screenWidth, currentY, bar, alpha);
            currentY += BAR_HEIGHT + BAR_SPACING;
        }
    }

    /**
     * 渲染单个状态条
     */
    private void renderStatusBar(GuiGraphics guiGraphics, int screenWidth, int y, StatusBar bar, float alpha) {
        float progressRatio = bar.currentProgress / bar.maxProgress;
        progressRatio = Mth.clamp(progressRatio, 0.0f, 1.0f);
        
        int barWidth = (int) BAR_WIDTH;
        int barHeight = (int) BAR_HEIGHT;
        int barX = (screenWidth - barWidth) / 2;
        
        // 计算颜色（根据进度值渐变）
        int colorIndex = (int) ((1.0f - progressRatio) * (PROGRESS_COLORS.length - 1));
        colorIndex = Mth.clamp(colorIndex, 0, PROGRESS_COLORS.length - 1);
        int progressColor = PROGRESS_COLORS[colorIndex];
        
        // 应用透明度
        int backgroundColor = applyAlpha(BACKGROUND_COLOR, alpha);
        int borderColor = applyAlpha(BORDER_COLOR, alpha);
        int filledColor = applyAlpha(progressColor, alpha);
        int textColor = applyAlpha(TEXT_COLOR, alpha);
        int shadowColor = applyAlpha(SHADOW_COLOR, alpha);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        
        // 绘制背景
        drawRoundedRect(guiGraphics, barX, y, barWidth, barHeight, backgroundColor);
        
        // 绘制进度条填充
        if (progressRatio > 0) {
            int filledWidth = (int) ((barWidth - 4) * progressRatio);
            drawRoundedRect(guiGraphics, barX + 2, y + 2, filledWidth, barHeight - 4, filledColor);
        }
        
        // 绘制边框
        drawBorder(guiGraphics, barX, y, barWidth, barHeight, borderColor);
        
        // 绘制文字
        String displayText = String.format("%s: %.0f/%.0f", bar.name, bar.currentProgress, bar.maxProgress);
        int textWidth = Minecraft.getInstance().font.width(displayText);
        int textX = (screenWidth - textWidth) / 2;
        int textY = y - 12;
        
        // 文字阴影
        guiGraphics.drawString(Minecraft.getInstance().font, displayText, textX + 1, textY + 1, shadowColor, false);
        
        // 文字主体
        guiGraphics.drawString(Minecraft.getInstance().font, displayText, textX, textY, textColor, false);
        
        // 绘制进度百分比（可选）
        String percentText = String.format("%.0f%%", progressRatio * 100);
        int percentWidth = Minecraft.getInstance().font.width(percentText);
        int percentX = (screenWidth - percentWidth) / 2;
        int percentY = y + barHeight + 2;
        
        guiGraphics.drawString(Minecraft.getInstance().font, percentText, percentX + 1, percentY + 1, shadowColor, false);
        guiGraphics.drawString(Minecraft.getInstance().font, percentText, percentX, percentY, textColor, false);
        
        poseStack.popPose();
    }

    /**
     * 绘制圆角矩形
     */
    private void drawRoundedRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        int radius = 3;
        
        // 主矩形
        guiGraphics.fill(x + radius, y, x + width - radius, y + height, color);
        guiGraphics.fill(x, y + radius, x + width, y + height - radius, color);
        

    }

    /**
     * 绘制四分之一圆（用于圆角）
     */

    /**
     * 绘制边框
     */
    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        int borderThickness = 1;
        
        // 上下边框
        guiGraphics.fill(x, y, x + width, y + borderThickness, color);
        guiGraphics.fill(x, y + height - borderThickness, x + width, y + height, color);
        
        // 左右边框
        guiGraphics.fill(x, y, x + borderThickness, y + height, color);
        guiGraphics.fill(x + width - borderThickness, y, x + width, y + height, color);
    }

    /**
     * 应用透明度到颜色
     */
    private int applyAlpha(int color, float alpha) {
        int a = (int) ((color >> 24 & 255) * alpha);
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * 清理过期的状态条
     */
    private void cleanupExpiredBars() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = removalTimers.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (currentTime > entry.getValue()) {
                statusBars.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    /**
     * 状态条数据类
     */
    private static class StatusBar {
        public String name = "";
        public float currentProgress = 0.0f;
        public float maxProgress = 100.0f;
        public long lastUpdateTime = 0L;
        public long duration = DEFAULT_DURATION;
    }
}