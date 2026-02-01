package dev.doctor4t.trainmurdermystery.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapDetailsRenderer {
    private static String mapId = "";
    private static String mapDescription = "";
    private static long displayStartTime = 0L;
    private static final long DISPLAY_DURATION = 5000L; // 5秒显示时间
    
    // 黑边参数
    private static final int BLACK_BAR_HEIGHT = 40;
    private static final long FADE_DURATION = 1000L; // 1秒淡入淡出时间

    public static void renderHud(Font font, @NotNull LocalPlayer player, GuiGraphics context, float delta) {
        if (mapId.isEmpty() || System.currentTimeMillis() - displayStartTime > DISPLAY_DURATION) {
            return; // 不显示，超过显示时间
        }

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        
        // 计算透明度
        long elapsed = System.currentTimeMillis() - displayStartTime;
        float alpha = 1.0f;
        if (elapsed < FADE_DURATION) {
            // 淡入
            alpha = (float) elapsed / FADE_DURATION;
        } else if (elapsed > DISPLAY_DURATION - FADE_DURATION) {
            // 淡出
            alpha = (float) (DISPLAY_DURATION - elapsed) / FADE_DURATION;
        }
        
        int alphaInt = (int) (alpha * 255);
        if (alphaInt <= 0) return;

        // 渲染顶部黑边
        context.fill(0, 0, screenWidth, BLACK_BAR_HEIGHT, alphaInt << 24 | 0x000000);
        
        // 渲染底部黑边
        context.fill(0, screenHeight - BLACK_BAR_HEIGHT, screenWidth, screenHeight, alphaInt << 24 | 0x000000);

        // 渲染地图名称（左上角）

        String mapNameKey = "map." + mapId + ".name";
        String mapName = Language.getInstance().getOrDefault(mapNameKey);
        if (mapName.equals(mapNameKey)) {
            // 如果没有翻译，使用mapId作为名称
            mapName = mapId;
        }
        
        // 使用更大更粗的字体渲染地图名称
        int mapNameWidth = font.width(mapName);
        int mapNameX = 10; // 左边距
        int mapNameY = 10; // 顶边距
        
        // 绘制阴影
        context.drawString(font, mapName, mapNameX + 1, mapNameY + 1, 0x000000, false);
        // 绘制主文本
        int mapNameColor = alphaInt << 24 | 0xFFFFFF;
        context.drawString(font, mapName, mapNameX, mapNameY, mapNameColor, false);

        // 渲染地图描述（在地图名称下方）
        String mapDescKey = "map." + mapId + ".desc";
        String mapDesc = Language.getInstance().getOrDefault(mapDescKey);
        if (!mapDesc.equals(mapDescKey)) { // 确实存在翻译
            // 分割长描述成多行
            List<FormattedCharSequence> lines = font.split(Component.literal(mapDesc), screenWidth - 20);
            
            int descY = mapNameY + font.lineHeight + 5; // 名称下方一点
            
            for (FormattedCharSequence line : lines) {
                if (descY >= BLACK_BAR_HEIGHT - 5) break; // 避免超出黑边
                
                context.drawString(font, line, mapNameX, descY, 0xCCCCCC, false);
                descY += font.lineHeight;
            }
        }
    }

    /**
     * 设置要显示的地图详情
     * 
     * @param mapId 地图ID
     * @param mapDescription 地图描述
     */
    public static void setMapDetails(String mapId, String mapDescription) {
        MapDetailsRenderer.mapId = mapId;
        MapDetailsRenderer.mapDescription = mapDescription;
        displayStartTime = System.currentTimeMillis();
    }

    /**
     * 清除当前显示的地图详情
     */
    public static void clearMapDetails() {
        mapId = "";
        mapDescription = "";
    }
}