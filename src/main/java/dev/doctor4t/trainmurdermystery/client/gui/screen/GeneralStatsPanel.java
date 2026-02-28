package dev.doctor4t.trainmurdermystery.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerStatsComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GeneralStatsPanel extends AbstractWidget {
    private final PlayerStatsComponent stats;
    private final int screenWidth;
    private final int screenHeight;
    private final List<Renderable> renderables = new ArrayList<>();
    private final List<GuiEventListener> children = new ArrayList<>();

    public GeneralStatsPanel(int x, int y, int width, int height, PlayerStatsComponent stats, int screenWidth, int screenHeight) {
        super(x, y, width, height, Component.empty());
        this.stats = stats;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        setupComponents();
    }

    private void addRenderable(Renderable renderable) {
        renderables.add(renderable);
        if (renderable instanceof GuiEventListener) {
            children.add((GuiEventListener) renderable);
        }
    }

    private void addWidget(GuiEventListener widget) {
        children.add(widget);
        if (widget instanceof Renderable) {
            renderables.add((Renderable) widget);
        }
    }

    private void setupComponents() {
        // 不需要创建子组件，所有渲染在 renderWidget 中完成
        // 保留内部组件作为渲染对象
        ResourceLocation skinTexture = getPlayerSkinTexture();
        if (skinTexture != null) {
            PlayerHeadComponent headComponent = new PlayerHeadComponent(
                    getX() + 10,
                    getY(),
                    32,
                    skinTexture
            );
            addRenderable(headComponent);
        }
        // 底部贴图
        BottomTextureComponent bottomTexture = new BottomTextureComponent(
                getX() + 10,
                screenHeight - 64 - 10,
                getWidth() - 2 * 10,
                64
        );
        addRenderable(bottomTexture);
    }

    private ResourceLocation getPlayerSkinTexture() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return null;
        }
        PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        if (playerInfo == null) {
            return null;
        }
        return playerInfo.getSkin().texture();
    }

    private String formatPlayTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return Component.translatable("screen." + TMM.MOD_ID + ".player_stats.time.days_hours_minutes", days, hours % 24, minutes % 60).getString();
        } else if (hours > 0) {
            return Component.translatable("screen." + TMM.MOD_ID + ".player_stats.time.hours_minutes", hours, minutes % 60).getString();
        } else if (minutes > 0) {
            return Component.translatable("screen." + TMM.MOD_ID + ".player_stats.time.minutes_seconds", minutes, seconds % 60).getString();
        } else {
            return Component.translatable("screen." + TMM.MOD_ID + ".player_stats.time.seconds", seconds).getString();
        }
    }

    private double getKdRatio(int kills, int deaths) {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    private double getWinRate(int wins, int gamesPlayed) {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) wins / gamesPlayed * 100.0;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 绘制面板背景（可选）
        // graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x40000000);

        int leftPanelX = getX();
        int leftPanelWidth = getWidth();
        int currentY = getY();
        int leftColumnX = leftPanelX + 10;

        // 玩家名称（如果没有头部）
        ResourceLocation skinTexture = getPlayerSkinTexture();
        if (skinTexture == null && Minecraft.getInstance().player != null) {
            String playerName = Minecraft.getInstance().player.getDisplayName().getString();
            int playerNameWidth = Minecraft.getInstance().font.width(playerName);
            int centeredX = leftColumnX + (leftPanelWidth - 20 - playerNameWidth) / 2; // 考虑左右10像素边距
            graphics.drawString(Minecraft.getInstance().font,
                    Minecraft.getInstance().player.getDisplayName().copy().withStyle(style -> style.withColor(0xFFFFA0).withBold(true)),
                    centeredX, currentY, 0xFFFFA0);
            currentY += Minecraft.getInstance().font.lineHeight + 15;
        } else if (skinTexture != null) {
            currentY += 32 + 15;
        }

        // 通用统计数据标题
        String statsTitle = Component.translatable("screen." + TMM.MOD_ID + ".player_stats.general_stats").getString();
        int titleWidth = Minecraft.getInstance().font.width(statsTitle);
        int centeredTitleX = leftColumnX + (leftPanelWidth - 20 - titleWidth) / 2;
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable("screen." + TMM.MOD_ID + ".player_stats.general_stats").withStyle(style -> style.withBold(true)),
                centeredTitleX, currentY, 0xFFFFFFFF);
        currentY += Minecraft.getInstance().font.lineHeight + 10;

        // 两列布局
        int columnWidth = (leftPanelWidth - 2 * 10) / 2 - 5;
        int rightColumnX = leftColumnX + columnWidth + 10;
        int columnStartY = currentY;

        // 左列数据
        drawStatLabelCentered(graphics, leftColumnX, columnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_play_time", formatPlayTime(stats.getTotalPlayTime()), columnWidth);
        columnStartY += 20;
        drawStatLabelCentered(graphics, leftColumnX, columnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_games_played", String.valueOf(stats.getTotalGamesPlayed()), columnWidth);
        columnStartY += 20;
        drawStatLabelCentered(graphics, leftColumnX, columnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_kills", String.valueOf(stats.getTotalKills()), columnWidth);
        columnStartY += 20;
        drawStatLabelCentered(graphics, leftColumnX, columnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_team_kills", String.valueOf(stats.getTotalTeamKills()), columnWidth);
        columnStartY += 20;
        drawStatLabelCentered(graphics, leftColumnX, columnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_deaths", String.valueOf(stats.getTotalDeaths()), columnWidth);
        columnStartY += 20;
        drawStatLabelCentered(graphics, leftColumnX, columnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_lovers_wins", String.valueOf(stats.getTotalLoversWins()), columnWidth);
        columnStartY += 20;

        // 右列数据
        int rightColumnStartY = currentY;
        drawStatLabelCentered(graphics, rightColumnX, rightColumnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_wins", String.valueOf(stats.getTotalWins()), columnWidth);
        rightColumnStartY += 20;
        drawStatLabelCentered(graphics, rightColumnX, rightColumnStartY, "screen." + TMM.MOD_ID + ".player_stats.total_losses", String.valueOf(stats.getTotalLosses()), columnWidth);
        rightColumnStartY += 20;
        drawStatLabelCentered(graphics, rightColumnX, rightColumnStartY, "screen." + TMM.MOD_ID + ".player_stats.win_rate", String.format("%.2f%%", getWinRate(stats.getTotalWins(), stats.getTotalGamesPlayed())), columnWidth);
        rightColumnStartY += 20;
        drawStatLabelCentered(graphics, rightColumnX, rightColumnStartY, "screen." + TMM.MOD_ID + ".player_stats.kd_ratio", String.format("%.2f", getKdRatio(stats.getTotalKills(), stats.getTotalDeaths())), columnWidth);
        rightColumnStartY += 20;

        // 阵营统计部分（从左列结束位置开始）
        currentY = columnStartY;
        currentY += 10;
        drawFactionStats(graphics, leftColumnX, currentY, leftPanelWidth - 20);

        // 渲染子组件（头部和底部贴图）
        for (Renderable renderable : renderables) {
            renderable.render(graphics, mouseX, mouseY, delta);
        }
    }

    private void drawFactionStats(GuiGraphics graphics, int x, int y, int width) {
        // 平民阵营统计
        drawFactionSection(graphics, x, y, width,
                "screen." + TMM.MOD_ID + ".player_stats.civilian_stats",
                "screen." + TMM.MOD_ID + ".player_stats.total_civilian_games",
                "screen." + TMM.MOD_ID + ".player_stats.total_civilian_wins",
                "screen." + TMM.MOD_ID + ".player_stats.civilian_win_rate",
                "screen." + TMM.MOD_ID + ".player_stats.civilian_kd_ratio",
                stats.getTotalCivilianGames(), stats.getTotalCivilianWins(),
                stats.getTotalCivilianKills(), stats.getTotalCivilianDeaths());

        y += 50;

        // 杀手阵营统计
        drawFactionSection(graphics, x, y, width,
                "screen." + TMM.MOD_ID + ".player_stats.killer_stats",
                "screen." + TMM.MOD_ID + ".player_stats.total_killer_games",
                "screen." + TMM.MOD_ID + ".player_stats.total_killer_wins",
                "screen." + TMM.MOD_ID + ".player_stats.killer_win_rate",
                "screen." + TMM.MOD_ID + ".player_stats.killer_kd_ratio",
                stats.getTotalKillerGames(), stats.getTotalKillerWins(),
                stats.getTotalKillerKills(), stats.getTotalKillerDeaths());

        y += 50;

        // 中立阵营统计
        drawFactionSection(graphics, x, y, width,
                "screen." + TMM.MOD_ID + ".player_stats.neutral_stats",
                "screen." + TMM.MOD_ID + ".player_stats.total_neutral_games",
                "screen." + TMM.MOD_ID + ".player_stats.total_neutral_wins",
                "screen." + TMM.MOD_ID + ".player_stats.neutral_win_rate",
                "screen." + TMM.MOD_ID + ".player_stats.neutral_kd_ratio",
                stats.getTotalNeutralGames(), stats.getTotalNeutralWins(),
                stats.getTotalNeutralKills(), stats.getTotalNeutralDeaths());

        y += 50;

        // 警长阵营统计
        drawFactionSection(graphics, x, y, width,
                "screen." + TMM.MOD_ID + ".player_stats.sheriff_stats",
                "screen." + TMM.MOD_ID + ".player_stats.total_sheriff_games",
                "screen." + TMM.MOD_ID + ".player_stats.total_sheriff_wins",
                "screen." + TMM.MOD_ID + ".player_stats.sheriff_win_rate",
                "screen." + TMM.MOD_ID + ".player_stats.sheriff_kd_ratio",
                stats.getTotalSheriffGames(), stats.getTotalSheriffWins(),
                stats.getTotalSheriffKills(), stats.getTotalSheriffDeaths());
    }

    private void drawFactionSection(GuiGraphics graphics, int x, int y, int width,
                                       String titleKey, String gamesKey, String winsKey, String winRateKey, String kdRatioKey,
                                       int games, int wins, int kills, int deaths) {
        // 绘制标题
        String title = Component.translatable(titleKey).getString();
        int titleWidth = Minecraft.getInstance().font.width(title);
        int titleX = x + (width - titleWidth) / 2;
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable(titleKey).withStyle(style -> style.withColor(0xFFFFFF).withBold(true)),
                titleX, y, 0xFFFFFF);

        // 两列布局
        int columnWidth = width / 2 - 5;
        int rightColumnX = x + columnWidth + 10;
        int lineY = y + 15;

        // 左列：场次和胜场
        drawStatLabelCentered(graphics, x, lineY, gamesKey, String.valueOf(games), columnWidth);
        lineY += 12;
        drawStatLabelCentered(graphics, x, lineY, winsKey, String.valueOf(wins), columnWidth);

        // 右列：胜率和K/D比
        lineY = y + 15;
        drawStatLabelCentered(graphics, rightColumnX, lineY, winRateKey,
                String.format("%.2f%%", getWinRate(wins, games)), columnWidth);
        lineY += 12;
        drawStatLabelCentered(graphics, rightColumnX, lineY, kdRatioKey,
                String.format("%.2f", getKdRatio(kills, deaths)), columnWidth);
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
        // 无需 narration
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private void drawStatLabel(GuiGraphics graphics, int x, int y, String translationKey, String value) {
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable(translationKey, value).withStyle(style -> style.withColor(0xFFCCCCCC)),
                x, y, 0xFFCCCCCC);
    }

    private void drawStatLabelCentered(GuiGraphics graphics, int x, int y, String translationKey, String value, int columnWidth) {
        String text = Component.translatable(translationKey, value).getString();
        int textWidth = Minecraft.getInstance().font.width(text);
        int centeredX = x + (columnWidth - textWidth) / 2;
        graphics.drawString(Minecraft.getInstance().font,
                Component.translatable(translationKey, value).withStyle(style -> style.withColor(0xFFCCCCCC)),
                centeredX, y, 0xFFCCCCCC);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * 渲染玩家头部的自定义组件
     */
    private static class PlayerHeadComponent extends AbstractWidget {
        private final ResourceLocation skinTexture;
        private final int size;

        public PlayerHeadComponent(int x, int y, int size, ResourceLocation skinTexture) {
            super(x, y, size, size, Component.empty());
            this.skinTexture = skinTexture;
            this.size = size;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (skinTexture == null) return;

            RenderSystem.enableBlend();
            graphics.pose().pushPose();
            graphics.pose().translate(getX(), getY(), 0);
            // 渲染头部（8x8 纹理区域，位于 8,8 到 16,16）
            graphics.blit(skinTexture, 0, 0, size, size, 8, 8, 8, 8, 64, 64);
            // 渲染头盔层（40,8 到 48,16）
            graphics.blit(skinTexture, 0, 0, size, size, 40, 8, 8, 8, 64, 64);
            graphics.pose().popPose();
            RenderSystem.disableBlend();
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
            // 无需 narration
        }
    }

    /**
     * 渲染底部贴图的自定义组件
     */
    private static class BottomTextureComponent extends AbstractWidget {
        private static final ResourceLocation TEXTURE = PlayerStatsScreen.ID; // 使用 PlayerStatsScreen 的 ID
        public BottomTextureComponent(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }
        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            if (TEXTURE == null) return;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.pose().pushPose();
            graphics.pose().translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0);
            int height = 254;
            int width = 497;
            float scale = 0.28f;
            graphics.pose().scale(scale, scale, 1f);
            int xOffset = 0;
            int yOffset = 0;
            graphics.innerBlit(TEXTURE, (int) (xOffset - width / 2f), (int) (xOffset + width / 2f), (int) (yOffset - height / 2f), (int) (yOffset + height / 2f), 0, 0, 1f, 0, 1f, 1f, 1f, 1f, 1f);
            graphics.pose().popPose();
            RenderSystem.disableBlend();
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narrationElementOutput) {
            // 无需 narration
        }
    }
}