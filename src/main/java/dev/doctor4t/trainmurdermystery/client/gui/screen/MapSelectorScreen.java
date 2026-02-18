// MapSelectorScreen.java
package dev.doctor4t.trainmurdermystery.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.MapVotingComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.data.MapConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class MapSelectorScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.tryBuild(TMM.MOD_ID,
            "textures/gui/demo_background.png");
    private static final int MAP_BOX_WIDTH = 140;
    private static final int MAP_BOX_HEIGHT = 180;
    private static final int MAP_SPACING = 20;

    private final List<MapOption> mapOptions = new ArrayList<>();
    private MapOption hoveredMap = null;
    private MapOption selectedMap = null;
    private float animationProgress = 0.0f;
    private float selectionAnimation = 0.0f;
    private int scrollOffset = 0;
    private boolean isScrolling = false;
    private float gridOffset = 0.0f;
    private long startTime = System.currentTimeMillis();

    // 现代化配色方案 - 统一卡片颜色
    private final int backgroundColor = 0xFF0F0F1A;
    private final int accentColor = 0xFF4CC9F0;
    private final int primaryColor = 0xFF2D2D5A;
    private final int cardColor = 0xFF1E1E3A;
    private final int hoverCardColor = 0xFF25254D;
    private final int selectedCardColor = 0xFF3A3A6E;
    private final int textColor = 0xFFF8F9FA;
    private final int secondaryTextColor = 0xFFB0B0D0;
    private final int borderColor = 0xFF4CC9F0;

    // 粒子效果列表
    private final List<Particle> particles = new ArrayList<>();

    // 投票计数映射
    private final java.util.Map<String, Integer> voteCounts = new java.util.HashMap<>();

    public MapSelectorScreen() {
        super(Component.translatable("gui.tmm.map_selector.title"));
        initMapOptions();
        initParticles();
    }

    private void initMapOptions() {
        mapOptions.clear();

        // 从动态配置加载地图选项
        List<MapConfig.MapEntry> configMaps = MapConfig.getInstance().getMaps();
        if (configMaps != null) {
            for (MapConfig.MapEntry entry : configMaps) {
                mapOptions.add(new MapOption(entry.getId(),
                        Component.translatable(entry.getDisplayName()).getString(),
                        Component.translatable(entry.getDescription()).getString(),
                        entry.getColor()));
            }
        } else {
            // 如果配置为空，使用默认配置
            mapOptions.add(new MapOption("random", Component.translatable("gui.tmm.map_selector.random").getString(),
                    Component.translatable("gui.tmm.map_selector.random.desc").getString(), 0xFF4CC9F0));
            mapOptions.add(new MapOption("areas1", Component.translatable("gui.tmm.map_selector.zeppelin").getString(),
                    Component.translatable("gui.tmm.map_selector.zeppelin.desc").getString(), 0xFF9D0208));
        }
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(
                    (float) (Math.random() * width),
                    (float) (Math.random() * height),
                    (float) (Math.random() * 2 + 1),
                    (float) (Math.random() * 0.5f + 0.2f)));
        }
    }

    @Override
    protected void init() {
        super.init();
        animationProgress = 0.0f;
        selectionAnimation = 0.0f;
        gridOffset = 0.0f;
        startTime = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        super.tick();
        // 更新网格偏移，实现向下移动效果
        gridOffset += 0.5f;
        if (gridOffset > 50)
            gridOffset = 0;

        // 更新粒子动画
        for (Particle particle : particles) {
            particle.update();
            if (particle.y > height) {
                particle.y = 0;
                particle.x = (float) (Math.random() * width);
            }
        }

        // 更新地图卡片的悬停动画
        for (MapOption map : mapOptions) {
            if (map == hoveredMap) {
                map.hoverTime = Mth.lerp(0.15f, map.hoverTime, 1.0f);
            } else {
                map.hoverTime = Mth.lerp(0.15f, map.hoverTime, 0.0f);
            }

            // 更新选中动画
            if (map == selectedMap) {
                map.selectionTime = Mth.lerp(0.1f, map.selectionTime, 1.0f);
            } else {
                map.selectionTime = Mth.lerp(0.1f, map.selectionTime, 0.0f);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        // 不调用父类的背景渲染，我们自己绘制
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // 平滑动画更新
        animationProgress = Mth.lerp(0.1f, animationProgress, 1.0f);

        // 绘制现代化背景
        renderModernBackground(guiGraphics, partialTicks);

        // 绘制标题
        float titleAlpha = Mth.clamp((animationProgress - 0.2f) * 5, 0, 1);
        int titleColor = (int) (titleAlpha * 255) << 24 | textColor;
        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.tmm.map_selector.title").withStyle(net.minecraft.ChatFormatting.BOLD),
                width / 2, 30, titleColor);

        // 绘制副标题

        // 绘制地图选项
        renderMapOptions(guiGraphics, mouseX, mouseY, partialTicks);

        // 绘制投票倒计时
        renderVotingTimer(guiGraphics);

        // 绘制底部信息
        if (selectedMap != null) {
            drawSelectionInfo(guiGraphics);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void renderModernBackground(GuiGraphics guiGraphics, float partialTicks) {
        // 纯色背景
        guiGraphics.fill(0, 0, width, height, backgroundColor);

        // 绘制动态网格背景
        drawDynamicGrid(guiGraphics);

        // 绘制粒子效果
        drawParticles(guiGraphics);

        // 绘制顶部渐变条
        drawTopGradient(guiGraphics);

        // 绘制底部渐变条
        drawBottomGradient(guiGraphics);
    }

    private void drawDynamicGrid(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        final var buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // 网格参数 - 增大范围
        int gridSize = 50;
        float gridAlpha = 0.1f;
        int gridColor = (int) (gridAlpha * 255) << 24 | 0x4CC9F0;
        int subGridColor = (int) (gridAlpha * 0.5f * 255) << 24 | 0x4CC9F0;

        // 计算超出屏幕的范围，以创建更大范围的网格
        int extraRange = 200; // 扩大网格范围
        int extendedWidth = width + extraRange * 2;
        int extendedHeight = height + extraRange * 2;

        // 主网格线 - 扩大范围
        for (int x = -extraRange; x <= extendedWidth; x += gridSize) {
            float yOffset = (gridOffset + x) % gridSize;
            buffer.addVertex(matrix, x, -extraRange - yOffset, 0).setColor(gridColor);
            buffer.addVertex(matrix, x, extendedHeight - yOffset, 0).setColor(gridColor);
        }

        for (int y = -extraRange; y <= extendedHeight; y += gridSize) {
            float xOffset = (gridOffset + y) % gridSize;
            buffer.addVertex(matrix, -extraRange - xOffset, y - gridOffset, 0).setColor(gridColor);
            buffer.addVertex(matrix, extendedWidth - xOffset, y - gridOffset, 0).setColor(gridColor);
        }

        // 子网格线（更细的线）- 扩大范围
        int subGridSize = gridSize / 2;
        for (int x = -extraRange + subGridSize; x < extendedWidth; x += gridSize) {
            float yOffset = (gridOffset + x + subGridSize) % gridSize;
            buffer.addVertex(matrix, x, -extraRange - yOffset, 0).setColor(subGridColor);
            buffer.addVertex(matrix, x, extendedHeight - yOffset, 0).setColor(subGridColor);
        }

        for (int y = -extraRange + subGridSize; y < extendedHeight; y += gridSize) {
            float xOffset = (gridOffset + y + subGridSize) % gridSize;
            buffer.addVertex(matrix, -extraRange - xOffset, y - gridOffset, 0).setColor(subGridColor);
            buffer.addVertex(matrix, extendedWidth - xOffset, y - gridOffset, 0).setColor(subGridColor);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        poseStack.popPose();
    }

    private void drawParticles(GuiGraphics guiGraphics) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        final var buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // 绘制粒子
        for (Particle particle : particles) {
            float size = particle.size;
            int alpha = (int) (particle.alpha * 255);
            int color = (alpha << 24) | 0x4CC9F0;

            buffer.addVertex(matrix, particle.x - size, particle.y - size, 0).setColor(color);
            buffer.addVertex(matrix, particle.x + size, particle.y - size, 0).setColor(color);
            buffer.addVertex(matrix, particle.x + size, particle.y + size, 0).setColor(color);
            buffer.addVertex(matrix, particle.x - size, particle.y + size, 0).setColor(color);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        poseStack.popPose();
    }

    private void drawTopGradient(GuiGraphics guiGraphics) {
        // 顶部渐变覆盖
        for (int y = 0; y < 60; y++) {
            float alpha = (60 - y) / 60.0f * 0.8f;
            int color = (int) (alpha * 255) << 24 | backgroundColor;
            guiGraphics.fill(0, y, width, y + 1, color);
        }
    }

    private void drawBottomGradient(GuiGraphics guiGraphics) {
        // 底部渐变覆盖
        for (int y = height - 80; y < height; y++) {
            float alpha = (y - (height - 80)) / 80.0f * 0.8f;
            int color = (int) (alpha * 255) << 24 | backgroundColor;
            guiGraphics.fill(0, y, width, y + 1, color);
        }
    }

    private void renderMapOptions(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // width;
        int totalWidth = mapOptions.size() * (MAP_BOX_WIDTH + MAP_SPACING);
        int startX = 0;// 水平滚动
        if (totalWidth + 40 >= width) {
            startX = 40 - scrollOffset;
        } else {
            startX = MAP_SPACING / 2 + width / 2 - totalWidth / 2;
        }
        int startY = (height - MAP_BOX_HEIGHT) / 2; // 垂直居中

        hoveredMap = null;

        for (int i = 0; i < mapOptions.size(); i++) {
            MapOption map = mapOptions.get(i);

            int x = startX + i * (MAP_BOX_WIDTH + MAP_SPACING); // 水平排列
            int y = startY; // 所有卡片在同一垂直位置

            // 检查鼠标悬停
            boolean isHovered = mouseX >= x && mouseX <= x + MAP_BOX_WIDTH &&
                    mouseY >= y && mouseY <= y + MAP_BOX_HEIGHT;

            if (isHovered) {
                hoveredMap = map;
            }

            // 绘制地图卡片
            drawMapCard(guiGraphics, map, x, y, isHovered, 1.0f);
        }
    }

    private void drawMapCard(GuiGraphics guiGraphics, MapOption map, int x, int y, boolean isHovered, float alpha) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // 悬停动画缩放
        float hoverScale = 1.0f + map.hoverTime * 0.03f;
        float selectionScale = 1.0f + map.selectionTime * 0.02f;
        float totalScale = hoverScale * selectionScale;

        float centerX = x + MAP_BOX_WIDTH / 2.0f;
        float centerY = y + MAP_BOX_HEIGHT / 2.0f;

        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(totalScale, totalScale, 1.0f);
        poseStack.translate(-centerX, -centerY, 0);

        // 计算颜色透明度
        int cardAlpha = (int) (alpha * 255);

        // 确定卡片背景颜色
        int cardBgColor;
        if (map == selectedMap) {
            float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.003) * 0.2 + 0.8);
            int pulseAlpha = (int) (cardAlpha * pulse);
            cardBgColor = (pulseAlpha << 24) | selectedCardColor;
        } else if (isHovered) {
            cardBgColor = (cardAlpha << 24) | hoverCardColor;
        } else {
            cardBgColor = (cardAlpha << 24) | cardColor;
        }

        // 绘制卡片背景（直角矩形）
        guiGraphics.fill(x, y, x + MAP_BOX_WIDTH, y + MAP_BOX_HEIGHT, cardBgColor);

        // 绘制边框
        float borderThickness = 1.5f + map.hoverTime * 2f + map.selectionTime * 3f;
        int borderAlpha = (int) (cardAlpha * (0.5f + map.hoverTime * 0.5f));
        int borderColor = (borderAlpha << 24) | 0x4CC9F0;

        // 绘制四周边框
        guiGraphics.fill(x, y, x + MAP_BOX_WIDTH, y + (int) borderThickness, borderColor); // 上边框
        guiGraphics.fill(x, y + MAP_BOX_HEIGHT - (int) borderThickness, x + MAP_BOX_WIDTH, y + MAP_BOX_HEIGHT,
                borderColor); // 下边框
        guiGraphics.fill(x, y, x + (int) borderThickness, y + MAP_BOX_HEIGHT, borderColor); // 左边框
        guiGraphics.fill(x + MAP_BOX_WIDTH - (int) borderThickness, y, x + MAP_BOX_WIDTH, y + MAP_BOX_HEIGHT,
                borderColor); // 右边框

        // 绘制顶部颜色条
        int topBarColor = map.color | 0xFF000000;
        guiGraphics.fill(x, y, x + MAP_BOX_WIDTH, y + 6, topBarColor);

        // 绘制地图名称（居中）
        int nameAlphaValue = (int) (cardAlpha * 0.9f);
        int nameColor = (nameAlphaValue << 24) | textColor;
        String truncatedName = font.plainSubstrByWidth(map.displayName, MAP_BOX_WIDTH - 20);
        guiGraphics.drawCenteredString(font, truncatedName,
                x + MAP_BOX_WIDTH / 2, y + 20, nameColor);

        // 绘制地图ID（居中）- 仅在创造模式下显示
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            int idAlpha = (int) (cardAlpha * 0.8f);
            int idColor = (idAlpha << 24) | secondaryTextColor;
            String truncatedId = font.plainSubstrByWidth(map.id, MAP_BOX_WIDTH - 20);
            guiGraphics.drawCenteredString(font, truncatedId,
                    x + MAP_BOX_WIDTH / 2, y + 40, idColor);
        }

        // 绘制地图预览图片区域（放大后的中心框）
        int imageSize = 100; // 增大图片尺寸
        int imageX = x + (MAP_BOX_WIDTH - imageSize) / 2;
        int imageY = y + 60; // 调整位置以适应更大的图片

        // 绘制地图图片背景
        int imageBgAlpha = (int) (cardAlpha * 0.7f);
        int imageBgColor = (imageBgAlpha << 24) | 0x2D2D5A;
        guiGraphics.fill(imageX, imageY, imageX + imageSize, imageY + imageSize, imageBgColor);

        // 绘制地图图片边框
        int imageBorderColor = (cardAlpha << 24) | borderColor;
        guiGraphics.fill(imageX, imageY, imageX + imageSize, imageY + 1, imageBorderColor); // 上边框
        guiGraphics.fill(imageX, imageY + imageSize - 1, imageX + imageSize, imageY + imageSize, imageBorderColor); // 下边框
        guiGraphics.fill(imageX, imageY, imageX + 1, imageY + imageSize, imageBorderColor); // 左边框
        guiGraphics.fill(imageX + imageSize - 1, imageY, imageX + imageSize, imageY + imageSize, imageBorderColor); // 右边框

        // 尝试绘制地图预览图片
        drawMapPreviewImage(guiGraphics, map.id, imageX, imageY, imageSize, imageSize);

        // 绘制地图描述（居中，位于底部）
        int descAlphaValue = (int) (cardAlpha * 0.7f);
        int descColor = (descAlphaValue << 24) | secondaryTextColor;
        String desc = map.description.length() > 25 ? map.description.substring(0, 25) + "..." : map.description;
        guiGraphics.drawCenteredString(font, desc,
                x + MAP_BOX_WIDTH / 2, y + MAP_BOX_HEIGHT - 45, descColor);

        // 绘制投票数量（居中，位于底部）
        int voteCount = getVoteCount(map.id);
        if (voteCount > 0) {
            String voteText = Component.translatable("gui.tmm.map_selector.vote_count", voteCount).getString();
            int voteAlpha = (int) (cardAlpha * 0.9f);
            int voteColor = (voteAlpha << 24) | accentColor;
            guiGraphics.drawCenteredString(font, voteText,
                    x + MAP_BOX_WIDTH / 2, y + MAP_BOX_HEIGHT - 30, voteColor);
        }

        // 绘制选择指示器
        if (map == selectedMap) {
            drawSelectionIndicator(guiGraphics, x, y, cardAlpha);
        }

        poseStack.popPose();
    }

    private void drawMapPreviewImage(GuiGraphics guiGraphics, String id, int imageX, int imageY, int imageSize,
            int imageSize1) {
        // 尝试绘制地图预览图片
        try {
            // 为每个地图ID创建特定的纹理位置
            ResourceLocation textureLocation = ResourceLocation.tryBuild(TMM.MOD_ID,
                    "textures/gui/maps/" + id + ".png");

            // 检查资源是否存在
            if (textureExists(textureLocation)) {
                // 绑定纹理并绘制
                RenderSystem.setShaderTexture(0, textureLocation);

                // 绘制纹理
                guiGraphics.blit(textureLocation, imageX + 2, imageY + 2, 0, 0,
                        imageSize - 4, imageSize1 - 4, imageSize - 4, imageSize1 - 4);
            } else {
                // 如果没有找到特定地图图片，绘制占位符
                drawPlaceholderImage(guiGraphics, id, imageX, imageY, imageSize, imageSize1);
            }
        } catch (Exception e) {
            // 发生错误时绘制占位符
            drawPlaceholderImage(guiGraphics, id, imageX, imageY, imageSize, imageSize1);
        }
    }

    private boolean textureExists(ResourceLocation resourceLocation) {
        try {
            return Minecraft.getInstance().getResourceManager().getResource(resourceLocation).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    private void drawPlaceholderImage(GuiGraphics guiGraphics, String id, int imageX, int imageY, int imageSize,
            int imageSize1) {
        // 绘制占位符背景
        int placeholderColor = 0x40888888;
        guiGraphics.fill(imageX + 2, imageY + 2, imageX + imageSize - 2, imageY + imageSize - 2, placeholderColor);

        // 绘制地图首字母
        String initial = id.isEmpty() ? "?" : String.valueOf(Character.toUpperCase(id.charAt(0)));
        int initialColor = 0x80FFFFFF;
        int textWidth = font.width(initial);
        int textX = imageX + (imageSize - textWidth) / 2;
        int textY = imageY + (imageSize - font.lineHeight) / 2;
        guiGraphics.drawString(font, initial, textX, textY, initialColor, false);
    }

    // 获取投票数量的方法（接口，具体实现由您完成）
    private int getVoteCount(String mapId) {
        // 如果投票系统激活，优先返回CCA组件中的数据
        if (minecraft != null && minecraft.level != null) {
            dev.doctor4t.trainmurdermystery.cca.MapVotingComponent votingComponent = dev.doctor4t.trainmurdermystery.cca.MapVotingComponent.KEY
                    .get(minecraft.level);
            if (votingComponent.isVotingActive()) {
                return votingComponent.getVoteCount(mapId);
            }
        }
        // 返回存储的投票数量，默认为0
        return voteCounts.getOrDefault(mapId, 0);
    }

    // 设置投票数量的方法（用于外部更新）
    public void setVoteCount(String mapId, int count) {
        voteCounts.put(mapId, count);
    }

    // 增加投票数量的方法
    public void addVote(String mapId) {
        int currentCount = getVoteCount(mapId);
        voteCounts.put(mapId, currentCount + 1);
    }

    private void drawSelectionIndicator(GuiGraphics guiGraphics, int x, int y, int alpha) {
        // 绘制选中动画 - 边框发光效果
        float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7); // 更快的脉冲效果
        int pulseAlpha = (int) (100 + pulse * 155);

        // 绘制顶部边框
        int borderWidth = 4; // 增加边框宽度
        int animatedTopColor = (pulseAlpha << 24) | accentColor;
        guiGraphics.fill(x - borderWidth, y - borderWidth,
                x + MAP_BOX_WIDTH + borderWidth, y,
                animatedTopColor);

        // 绘制右侧边框
        guiGraphics.fill(x + MAP_BOX_WIDTH, y - borderWidth,
                x + MAP_BOX_WIDTH + borderWidth, y + MAP_BOX_HEIGHT + borderWidth,
                animatedTopColor);

        // 绘制底部边框
        guiGraphics.fill(x - borderWidth, y + MAP_BOX_HEIGHT,
                x + MAP_BOX_WIDTH + borderWidth, y + MAP_BOX_HEIGHT + borderWidth,
                animatedTopColor);

        // 绘制左侧边框
        guiGraphics.fill(x - borderWidth, y - borderWidth,
                x, y + MAP_BOX_HEIGHT + borderWidth,
                animatedTopColor);

        // 绘制移动的“灯带”效果
        long time = System.currentTimeMillis();
        float waveOffset = (time * 0.01f) % (2 * MAP_BOX_WIDTH + 2 * MAP_BOX_HEIGHT);

        // 绘制移动亮点
        int highlightSize = 6;
        int highlightAlpha = (int) (255 * (0.8 + 0.2 * Math.sin(time * 0.01))); // 额外亮度脉冲
        int highlightColor = (highlightAlpha << 24) | 0xFFFFFFFF;

        // 计算亮点位置（沿边框循环移动）
        if (waveOffset < MAP_BOX_WIDTH) {
            // 顶部边框
            int highlightX = (int) (x + waveOffset);
            guiGraphics.fill(highlightX, y - borderWidth,
                    highlightX + highlightSize, y,
                    highlightColor);
        } else if (waveOffset < MAP_BOX_WIDTH + MAP_BOX_HEIGHT) {
            // 右侧边框
            int highlightX = x + MAP_BOX_WIDTH;
            int highlightY = (int) (y + waveOffset - MAP_BOX_WIDTH);
            guiGraphics.fill(highlightX, highlightY,
                    highlightX + borderWidth, highlightY + highlightSize,
                    highlightColor);
        } else if (waveOffset < 2 * MAP_BOX_WIDTH + MAP_BOX_HEIGHT) {
            // 底部边框
            int highlightX = (int) (x + MAP_BOX_WIDTH - (waveOffset - MAP_BOX_WIDTH - MAP_BOX_HEIGHT));
            int highlightY = y + MAP_BOX_HEIGHT;
            guiGraphics.fill(highlightX, highlightY,
                    highlightX + highlightSize, highlightY + borderWidth,
                    highlightColor);
        } else {
            // 左侧边框
            int highlightX = x - borderWidth;
            int highlightY = (int) (y + MAP_BOX_HEIGHT - (waveOffset - 2 * MAP_BOX_WIDTH - MAP_BOX_HEIGHT));
            if (highlightY > y - borderWidth) {
                guiGraphics.fill(highlightX, highlightY,
                        highlightX + borderWidth, highlightY + highlightSize,
                        highlightColor);
            }
        }

        // 绘制选中图标
        float iconPulse = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7);
        int iconAlpha = (int) (alpha * iconPulse);
        int checkColor = (iconAlpha << 24) | accentColor;

        // 绘制选中背景
        int checkBgSize = 24;
        int checkBgX = x + MAP_BOX_WIDTH - checkBgSize - 5;
        int checkBgY = y + 5;
        int checkBgAlpha = (int) (alpha * 0.8f);
        int checkBgColor = (checkBgAlpha << 24) | 0xFF000000;
        guiGraphics.fill(checkBgX, checkBgY, checkBgX + checkBgSize, checkBgY + checkBgSize, checkBgColor);

        // 绘制对勾
        String checkmark = "✓";
        int textX = checkBgX + (checkBgSize - font.width(checkmark)) / 2;
        int textY = checkBgY + (checkBgSize - 9) / 2;
        guiGraphics.drawString(font, checkmark, textX, textY, checkColor, false);
    }

    private void drawSelectionInfo(GuiGraphics guiGraphics) {
        int infoY = height - 80;
        int infoHeight = 80;

        // 绘制底部信息栏背景
        for (int y = infoY; y < height; y++) {
            float alpha = (y - infoY) / (float) infoHeight * 0.7f;
            int color = (int) (alpha * 255) << 24 | backgroundColor;
            guiGraphics.fill(0, y, width, y + 1, color);
        }

        // 绘制选中地图信息
        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.tmm.map_selector.selected", selectedMap.displayName)
                        .withStyle(net.minecraft.ChatFormatting.BOLD),
                width / 2, infoY + 15, accentColor);

        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.tmm.map_selector.map_id", selectedMap.id),
                width / 2, infoY + 35, secondaryTextColor);

        guiGraphics.drawCenteredString(font,
                Component.translatable("gui.tmm.map_selector.confirm_prompt"),
                width / 2, infoY + 55, 0xFF8888AA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hoveredMap != null && button == 0) {
            // 如果投票活跃，记录投票
            if (minecraft.level != null) {
                dev.doctor4t.trainmurdermystery.cca.MapVotingComponent votingComponent = dev.doctor4t.trainmurdermystery.cca.MapVotingComponent.KEY
                        .get(minecraft.level);
                if (votingComponent.isVotingActive()) {
                    if (minecraft.player != null) {
                        // 通过网络包发送投票信息到服务器
                        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
                                .send(new dev.doctor4t.trainmurdermystery.network.VoteForMapPayload(hoveredMap.id));
                        // 显示投票成功的提示
                        if (minecraft.player != null) {
                            minecraft.player.displayClientMessage(
                                    Component.translatable("gui.tmm.map_selector.selected", hoveredMap.displayName)
                                            .withStyle(net.minecraft.ChatFormatting.GREEN),
                                    false);
                        }
                    }
                }
            }

            // 如果点击的是已选择的地图，则取消选择
            if (selectedMap == hoveredMap) {
                selectedMap = null;
            } else {
                selectedMap = hoveredMap;
            }
            playClickSound();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        scrollOffset = Mth.clamp(scrollOffset + (int) (-deltaY * 20), 0, getMaxScroll());
        return true;
    }

    private int getMaxScroll() {
        int totalWidth = mapOptions.size() * (MAP_BOX_WIDTH + MAP_SPACING);
        int visibleWidth = width - 80; // 考虑左右边距
        return Math.max(0, totalWidth - visibleWidth);
    }

    private void playClickSound() {
        // 播放选择音效
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    0.3f, 1.0f + (float) Math.random() * 0.2f);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // Enter
            confirmSelection();
            return true;
        } else if (keyCode == 256) { // ESC
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void confirmSelection() {
        if (selectedMap != null) {
            // 播放确认音效
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                        0.5f, 1.0f);
            }

            // 如果投票活跃，玩家不能直接确认选择，只能投票
            if (minecraft.level != null) {
                dev.doctor4t.trainmurdermystery.cca.MapVotingComponent votingComponent = dev.doctor4t.trainmurdermystery.cca.MapVotingComponent.KEY
                        .get(minecraft.level);
                if (votingComponent.isVotingActive()) {
                    // 在投票期间，点击地图会增加投票
                    if (minecraft.player != null) {
                        // 通过网络包发送投票信息到服务器
                        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
                                .send(new dev.doctor4t.trainmurdermystery.network.VoteForMapPayload(selectedMap.id));
                        minecraft.player.displayClientMessage(
                                Component.translatable("gui.tmm.map_selector.voted_for", selectedMap.displayName)
                                        .withStyle(net.minecraft.ChatFormatting.GREEN),
                                false);
                    }
                } else {
                    // 如果不在投票期间，显示普通消息
                    minecraft.player.displayClientMessage(
                            Component.translatable("gui.tmm.map_selector.selected", selectedMap.displayName)
                                    .withStyle(net.minecraft.ChatFormatting.GREEN),
                            false);
                }
            }

            onClose();
        }
    }

    private void renderVotingTimer(GuiGraphics guiGraphics) {
        MapVotingComponent votingManager = dev.doctor4t.trainmurdermystery.cca.MapVotingComponent.KEY
                .get(minecraft.level);
        if (votingManager.isVotingActive()) {
            int timeLeft = votingManager.getVotingTimeLeft() / 20;
            String timerText = Component.translatable("gui.tmm.map_selector.voting_timer", timeLeft).getString();

            // 计算文本位置 - 屏幕顶部中央，与标题保持相同间隔
            int textWidth = font.width(timerText);
            int textX = (width - textWidth) / 2;
            int textY = 50; // 与标题保持相同间隔

            // 绘制背景矩形
            int bgColor = 0x80000000; // 半透明黑色背景
            guiGraphics.fill(textX - 10, textY - 5, textX + textWidth + 10, textY + font.lineHeight + 5, bgColor);

            // 绘制倒计时文本
            guiGraphics.drawString(font, timerText, textX, textY, 0xFFFFFF);

            // 绘制提示文本
            String hintText = Component.translatable("gui.tmm.map_selector.voting_active").getString();
            int hintWidth = font.width(hintText);
            int hintX = (width - hintWidth) / 2;
            int hintY = textY + font.lineHeight + 5; // 减少与计时器的间隔

            guiGraphics.drawString(font, hintText, hintX, hintY, 0xFFFF00); // 黄色提示
        }
    }

    @Override
    public void onClose() {
        // 播放关闭音效
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    0.2f, 0.8f);
        }
        super.onClose();
    }

    // 地图选项内部类
    private static class MapOption {
        final String id;
        final String displayName;
        final String description;
        final int color;
        float hoverTime = 0.0f;
        float selectionTime = 0.0f;

        MapOption(String id, String displayName, String description, int color) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.color = color;
        }
    }

    // 粒子效果类
    private static class Particle {
        float x;
        float y;
        float speed;
        float size;
        float alpha;

        Particle(float x, float y, float speed, float size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
            this.alpha = (float) (Math.random() * 0.3 + 0.2);
        }

        void update() {
            y += speed;
            // 轻微的水平漂移
            x += (float) (Math.sin(System.currentTimeMillis() * 0.001 + y * 0.01) * 0.2);
            // 轻微的透明度变化
            alpha = (float) (0.2 + 0.3 * Math.sin(System.currentTimeMillis() * 0.002 + y * 0.02));
        }
    }
}