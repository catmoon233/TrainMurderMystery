package dev.doctor4t.trainmurdermystery.client.gui;

import dev.doctor4t.trainmurdermystery.data.MapConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapDetailsRenderer {
    public static String mapId = "";
    public static String mapDescription = "";
    public static String mapAuthor = "";
    private static long displayStartTime = 0L;
    private static final long DISPLAY_DURATION = 7000L; // 8秒显示时间
    private static final long FADE_DURATION = 1000L; // 1秒淡入淡出时间

    // 背景参数
    private static final int BACKGROUND_HEIGHT = 100;
    private static final int BACKGROUND_COLOR = 0x80000000; // 半透明黑色背景
    private static final int ACCENT_COLOR = 0xFF4A90E2; // 强调色（蓝色）
    private static final int BORDER_COLOR = 0xFF555555; // 边框颜色

    // 动画参数
    private static float slideOffset = 0f;
    private static final float SLIDE_DISTANCE = 50f; // 滑动距离

    public static void renderHud(Font font, @NotNull LocalPlayer player, GuiGraphics context, float delta) {
        if (mapId.isEmpty() || System.currentTimeMillis() - displayStartTime > DISPLAY_DURATION) {
            return; // 不显示，超过显示时间
        }

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();

        // 计算透明度和动画进度
        long elapsed = System.currentTimeMillis() - displayStartTime;
        float alpha = 1.0f;
        float slideProgress = 1.0f;

        if (elapsed < FADE_DURATION) {
            // 淡入 + 滑动进入
            alpha = (float) elapsed / FADE_DURATION;
            slideProgress = alpha;
            slideOffset = SLIDE_DISTANCE * (1 - alpha);
        } else if (elapsed > DISPLAY_DURATION - FADE_DURATION) {
            // 淡出
            alpha = (float) (DISPLAY_DURATION - elapsed) / FADE_DURATION;
            slideOffset = 0f;
        } else {
            // 完全显示
            alpha = 1.0f;
            slideOffset = 0f;
        }

        int alphaInt = (int) (alpha * 255);
        if (alphaInt <= 0) return;

        // 保存当前矩阵状态
        context.pose().pushPose();

        // 应用滑动动画
        context.pose().translate(0, slideOffset, 0);

        // 渲染背景面板（圆角设计）
        int panelWidth = screenWidth / 2; // 半宽面板
        int panelX = 10; // 左边距
        int panelY = 10; // 顶边距
        int panelHeight = BACKGROUND_HEIGHT;

        // 绘制背景（半透明黑色，带边框）
        drawRoundedPanel(context, panelX, panelY, panelWidth, panelHeight,
                alphaInt << 24 | 0x000000, BORDER_COLOR, 8);

        // 绘制装饰性左侧条
        int accentBarWidth = 6;
        int accentBarHeight = panelHeight - 10;
        context.fill(panelX + 5, panelY + 5,
                panelX + 5 + accentBarWidth,
                panelY + 5 + accentBarHeight,
                (alphaInt << 24) | ACCENT_COLOR);

        // 获取地图信息
        AtomicReference<String> mapNameKey = new AtomicReference<>("map." + mapId + ".name");
        AtomicReference<String> mapAuthorKey = new AtomicReference<>("map." + mapId + ".author");
        AtomicReference<String> mapDescKey = new AtomicReference<>("map." + mapId + ".desc");

        MapConfig.getInstance().getMaps().stream()
                .filter(map -> map.id.equals(mapId))
                .findFirst()
                .ifPresent(map -> {
                    mapNameKey.set(map.displayName);
                    // 假设地图配置中有作者字段，如果没有则使用默认
                    mapAuthorKey.set("allinYOKYO canyuesama haiman wifi_left guanzheqwq biantwin");
                    mapDescKey.set(map.description);
                });

        // 渲染地图名称（放大标题）
        String mapName = Language.getInstance().getOrDefault(mapNameKey.get());
        if (mapName.equals(mapNameKey.get())) {
            mapName = mapId;
        }

        // 使用大号字体渲染地图名称（带阴影效果）
        int mapNameX = panelX + 20;
        int mapNameY = panelY + 15;

        // 标题阴影
        context.drawString(font, mapName, mapNameX + 2, mapNameY + 2, 0x80000000, false);

        // 主标题（大字体，通过缩放实现）
        context.pose().pushPose();
        float titleScale = 1.3f; // 标题放大30%
        context.pose().translate(mapNameX, mapNameY, 0);
        context.pose().scale(titleScale, titleScale, 1.0f);

        // 标题渐变颜色（顶部亮，底部暗）
        int titleColorTop = (alphaInt << 24) | 0xFFFFFF; // 白色
        int titleColorBottom = (alphaInt << 24) | 0xAAAAAA; // 浅灰色

        // 简单渐变效果
        context.drawString(font, mapName, 0, 0, titleColorTop, false);
        context.pose().popPose();

        // 渲染作者信息（在地图名称下方）
        Component author = Component.translatable(mapAuthorKey.get());
//        if (!author.equals(mapAuthorKey.get()) || !author.equals("Unknown Author")) {
            int authorX = mapNameX;
            int authorY = mapNameY + (int)(font.lineHeight * titleScale) + 5;

            // 作者图标（可选的emoji或符号）
            String authorPrefix = "👤 "; // 或者使用 "✍️ " 或 "📝 "

            // 作者文本（斜体，灰色）
            MutableComponent authorText = Component.literal(authorPrefix + author)
                    .withStyle(Style.EMPTY.withItalic(true).withColor(0xFFAAAAAA));

            // 绘制作者
            context.drawString(font, authorText, authorX, authorY, (alphaInt << 24) | 0xAAAAAA, false);
//        }

        // 渲染地图描述（作者下方，小字体）
        String mapDesc = Language.getInstance().getOrDefault(mapDescKey.get());
        if (!mapDesc.equals(mapDescKey.get())) {
            // 描述区域
            int descX = mapNameX;
            int descY = panelY + 60; // 在作者下方，给描述留出空间

            // 描述的最大宽度
            int maxDescWidth = panelWidth - 30;

            // 分割长描述成多行
            List<FormattedCharSequence> lines = font.split(Component.literal(mapDesc), maxDescWidth);

            // 使用小号字体渲染描述
            int lineSpacing = 8; // 较小的行间距
            int linesToShow = Math.min(3, lines.size()); // 最多显示3行

            for (int i = 0; i < linesToShow; i++) {
                if (descY >= panelY + panelHeight - 10) break; // 避免超出面板

                FormattedCharSequence line = lines.get(i);

                // 如果是最后一行且有多行，添加省略号
                if (i == linesToShow - 1 && lines.size() > linesToShow) {
                    String text = line.toString();
                    if (text.length() > 0) {
                        text = text.substring(0, Math.max(0, text.length() - 3)) + "...";
                        line = FormattedCharSequence.forward(text, Style.EMPTY);
                    }
                }

                // 绘制描述行（小字体，灰色）
                context.drawString(font, line, descX, descY,
                        (alphaInt << 24) | 0xCCCCCC, false);
                descY += lineSpacing;
            }

            // 如果描述超过3行，显示提示
            if (lines.size() > 3) {
                Component moreHint = Component.literal("...")
                        .withStyle(Style.EMPTY.withItalic(true).withColor(0xFF888888));
                context.drawString(font, moreHint, descX + font.width("...") + 5,
                        descY - lineSpacing, (alphaInt << 24) | 0x888888, false);
            }
        }

        // 渲染地图ID标签（小字，右上角）
        String mapIdDisplay = "ID: " + mapId;
        int mapIdWidth = font.width(mapIdDisplay);
        int mapIdX = panelX + panelWidth - mapIdWidth - 10;
        int mapIdY = panelY + 10;

        context.drawString(font, mapIdDisplay, mapIdX, mapIdY,
                (alphaInt << 24) | 0x888888, false);

        // 渲染进度条（显示剩余时间）
        int progressBarWidth = panelWidth - 40;
        int progressBarX = panelX + 20;
        int progressBarY = panelY + panelHeight - 15;
        int progressBarHeight = 3;

        // 进度条背景
        context.fill(progressBarX, progressBarY,
                progressBarX + progressBarWidth,
                progressBarY + progressBarHeight,
                (alphaInt << 24) | 0x444444);

        // 进度条前景（根据剩余时间变化）
        float timeProgress = 1.0f - (float) elapsed / DISPLAY_DURATION;
        int progressWidth = (int) (progressBarWidth * timeProgress);

        // 进度条颜色渐变（从绿到黄到红）
        int progressColor = getProgressColor(timeProgress);
        context.fill(progressBarX, progressBarY,
                progressBarX + progressWidth,
                progressBarY + progressBarHeight,
                (alphaInt << 24) | progressColor);

        // 恢复矩阵状态
        context.pose().popPose();
    }

    /**
     * 绘制圆角面板
     */
    private static void drawRoundedPanel(GuiGraphics context, int x, int y, int width, int height,
                                         int color, int borderColor, int radius) {
        // 主背景
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);

        // 圆角区域（简化实现）
        fillCircleQuadrant(context, x + radius, y + radius, radius, 0, color); // 左上
        fillCircleQuadrant(context, x + width - radius, y + radius, radius, 1, color); // 右上
        fillCircleQuadrant(context, x + radius, y + height - radius, radius, 2, color); // 左下
        fillCircleQuadrant(context, x + width - radius, y + height - radius, radius, 3, color); // 右下

        // 边框（简化实现）
        // 上边框
        context.fill(x + radius, y, x + width - radius, y + 1, borderColor);
        // 下边框
        context.fill(x + radius, y + height - 1, x + width - radius, y + height, borderColor);
        // 左边框
        context.fill(x, y + radius, x + 1, y + height - radius, borderColor);
        // 右边框
        context.fill(x + width - 1, y + radius, x + width, y + height - radius, borderColor);

        // 圆角边框（简化）
        drawCircleQuadrantBorder(context, x + radius, y + radius, radius, 0, borderColor); // 左上
        drawCircleQuadrantBorder(context, x + width - radius, y + radius, radius, 1, borderColor); // 右上
        drawCircleQuadrantBorder(context, x + radius, y + height - radius, radius, 2, borderColor); // 左下
        drawCircleQuadrantBorder(context, x + width - radius, y + height - radius, radius, 3, borderColor); // 右下
    }

    /**
     * 填充四分之一圆（简化实现）
     */
    private static void fillCircleQuadrant(GuiGraphics context, int centerX, int centerY,
                                           int radius, int quadrant, int color) {
        // 简化实现：绘制一个小的正方形区域作为圆角
        int x = centerX - radius;
        int y = centerY - radius;

        switch (quadrant) {
            case 0: // 左上
                for (int i = 0; i < radius; i++) {
                    for (int j = 0; j < radius; j++) {
                        if (i + j < radius) {
                            context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
                        }
                    }
                }
                break;
            case 1: // 右上
                for (int i = 0; i < radius; i++) {
                    for (int j = 0; j < radius; j++) {
                        if (i + j < radius) {
                            context.fill(centerX + i, y + j, centerX + i + 1, y + j + 1, color);
                        }
                    }
                }
                break;
            case 2: // 左下
                for (int i = 0; i < radius; i++) {
                    for (int j = 0; j < radius; j++) {
                        if (i + j < radius) {
                            context.fill(x + i, centerY + j, x + i + 1, centerY + j + 1, color);
                        }
                    }
                }
                break;
            case 3: // 右下
                for (int i = 0; i < radius; i++) {
                    for (int j = 0; j < radius; j++) {
                        if (i + j < radius) {
                            context.fill(centerX + i, centerY + j, centerX + i + 1, centerY + j + 1, color);
                        }
                    }
                }
                break;
        }
    }

    /**
     * 绘制四分之一圆的边框
     */
    private static void drawCircleQuadrantBorder(GuiGraphics context, int centerX, int centerY,
                                                 int radius, int quadrant, int color) {
        // 简化实现：绘制圆角的边框像素
        for (int i = 0; i < radius; i++) {
            int j = radius - i - 1;

            switch (quadrant) {
                case 0: // 左上
                    context.fill(centerX - i - 1, centerY - j - 1, centerX - i, centerY - j, color);
                    context.fill(centerX - j - 1, centerY - i - 1, centerX - j, centerY - i, color);
                    break;
                case 1: // 右上
                    context.fill(centerX + i, centerY - j - 1, centerX + i + 1, centerY - j, color);
                    context.fill(centerX + j, centerY - i - 1, centerX + j + 1, centerY - i, color);
                    break;
                case 2: // 左下
                    context.fill(centerX - i - 1, centerY + j, centerX - i, centerY + j + 1, color);
                    context.fill(centerX - j - 1, centerY + i, centerX - j, centerY + i + 1, color);
                    break;
                case 3: // 右下
                    context.fill(centerX + i, centerY + j, centerX + i + 1, centerY + j + 1, color);
                    context.fill(centerX + j, centerY + i, centerX + j + 1, centerY + i + 1, color);
                    break;
            }
        }
    }

    /**
     * 根据进度获取颜色（从绿到黄到红）
     */
    private static int getProgressColor(float progress) {
        if (progress > 0.66f) {
            // 绿色 -> 青色
            float t = (progress - 0.66f) / 0.34f;
            return interpolateColor(0xFF00FF00, 0xFF00FFFF, t);
        } else if (progress > 0.33f) {
            // 黄色 -> 绿色
            float t = (progress - 0.33f) / 0.33f;
            return interpolateColor(0xFFFFFF00, 0xFF00FF00, t);
        } else {
            // 红色 -> 黄色
            float t = progress / 0.33f;
            return interpolateColor(0xFFFF0000, 0xFFFFFF00, t);
        }
    }

    /**
     * 插值颜色
     */
    private static int interpolateColor(int color1, int color2, float t) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * 设置要显示的地图详情
     *
     * @param mapId 地图ID
     * @param mapDescription 地图描述
     * @param mapAuthor 地图作者（可选）
     */
    public static void setMapDetails(String mapId, String mapDescription, String mapAuthor) {
        MapDetailsRenderer.mapId = mapId;
        MapDetailsRenderer.mapDescription = mapDescription;
        MapDetailsRenderer.mapAuthor = mapAuthor != null ? mapAuthor : "";
        displayStartTime = System.currentTimeMillis();

        // 重置动画
        slideOffset = SLIDE_DISTANCE;
    }

    /**
     * 设置要显示的地图详情（兼容旧版本）
     */
    public static void setMapDetails(String mapId, String mapDescription) {
        setMapDetails(mapId, mapDescription, "");
    }

    /**
     * 清除当前显示的地图详情
     */
    public static void clearMapDetails() {
        mapId = "";
        mapDescription = "";
        mapAuthor = "";
    }

    /**
     * 触发显示当前地图详情
     * 此方法会自动获取当前游戏中的地图信息并显示
     * @param mapId 地图ID
     */
    public static void triggerMapDetails(String mapId) {
        // 尝试从地图配置中获取地图详细信息
        AtomicReference<String> displayName = new AtomicReference<>(mapId);
        AtomicReference<String> description = new AtomicReference<>("");
        AtomicReference<String> author = new AtomicReference<>("");
        displayStartTime = 0;
        // 查找地图配置
        MapConfig.getInstance().getMaps().stream()
                .filter(map -> map.id.equals(mapId))
                .findFirst()
                .ifPresent(map -> {
                    displayName.set(map.displayName);
                    description.set(map.description);
                    // 使用默认作者列表
                    author.set("allinYOKYO canyuesama haiman wifi_left guanzheqwq biantwin");
                });

        // 设置地图详情并触发显示
        setMapDetails(mapId, description.get(), author.get());
    }
}