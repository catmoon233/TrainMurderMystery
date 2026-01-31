package dev.doctor4t.trainmurdermystery.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SkinSelectionList extends ObjectSelectionList<SkinSelectionList.SkinEntry> {
    private final SkinManagementScreen parentScreen;
    private final ItemStack itemStack;
    private final PlayerSkinsComponent skinsComponent;
    private final ItemRenderer itemRenderer;

    // 颜色定义
    private static final int BACKGROUND_COLOR = 0x90303030;
    private static final int BORDER_COLOR = 0xFF555555;
    private static final int SELECTED_COLOR = 0x40208020;
    private static final int HOVER_COLOR = 0x40404040;

    // 角色颜色映射
    private static final int[] ROLE_COLORS = {
            0xFFE74C3C, // 红色
            0xFF3498DB, // 蓝色
            0xFF2ECC71, // 绿色
            0xFFF39C12, // 橙色
            0xFF9B59B6, // 紫色
            0xFF1ABC9C, // 青色
            0xFFE67E22, // 深橙色
            0xFF95A5A6, // 灰色
    };

    public SkinSelectionList(SkinManagementScreen parentScreen, Minecraft mc, int width, int height, int top, int bottom, int itemHeight, ItemStack itemStack, PlayerSkinsComponent skinsComponent) {
        super(mc, width, height, top, bottom);
        this.parentScreen = parentScreen;
        this.itemStack = itemStack;
        this.skinsComponent = skinsComponent;
        this.itemRenderer = mc.getItemRenderer();

        // 设置正确的渲染位置


        // 获取物品类型名称
        String itemTypeName = getItemTypeName();

        // 添加默认皮肤
        this.addEntry(new SkinEntry("default"));

        // 添加已解锁的皮肤
        Map<String, Boolean> unlockedSkins = skinsComponent.getUnlockedSkinsForItemType(itemTypeName);
        for (Map.Entry<String, Boolean> entry : unlockedSkins.entrySet()) {
            if (entry.getValue()) {
                this.addEntry(new SkinEntry(entry.getKey()));
            }
        }
    }

    private String getItemTypeName() {
        // 获取物品类型名称的简化版本
        String className = itemStack.getItem().getClass().getSimpleName();
        // 移除"Item"后缀并转为小写
        return className.replace("Item", "").toLowerCase();
    }

    @Override
    public int getRowWidth() {
        return this.width - 20; // 留出滚动条空间
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRight() - 6; // 滚动条位置
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制列表背景
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, BACKGROUND_COLOR);

        // 绘制边框
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, BORDER_COLOR);
        guiGraphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, BORDER_COLOR);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, BORDER_COLOR);
        guiGraphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, BORDER_COLOR);

        // 调用父类渲染
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        // 不绘制额外的背景，已经在renderWidget中绘制
    }

    public class SkinEntry extends Entry<SkinEntry> {
        private final String skinName;
        private boolean hovered;
        private float hoverAnimation = 0f;
        private int skinColor;

        public SkinEntry(String skinName) {
            this.skinName = skinName;
            // 根据皮肤名称生成稳定的颜色
            this.skinColor = ROLE_COLORS[Math.abs(skinName.hashCode()) % ROLE_COLORS.length];
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTick) {

            this.hovered = isMouseOver;
            float targetHover = hovered ? 1f : 0f;
            hoverAnimation = Mth.lerp(0.2f, hoverAnimation, targetHover);

            // 获取当前装备的皮肤
            String itemTypeName = getItemTypeName();
            String currentSkin = skinsComponent.getEquippedSkinForItemType(itemTypeName);
            boolean isCurrent = skinName.equals(currentSkin);

            // 计算渲染位置
            int renderLeft = left;
            int renderTop = top;
            int renderWidth = width;
            int renderHeight = height;

            // 悬停动画效果
            if (hoverAnimation > 0) {
                float scale = 1 + hoverAnimation * 0.02f;
                renderWidth = (int)(width * scale);
                renderHeight = (int)(height * scale);
                renderLeft = left - (renderWidth - width) / 2;
                renderTop = top - (renderHeight - height) / 2;
            }

            // 绘制条目背景
            drawEntryBackground(guiGraphics, renderLeft, renderTop, renderWidth, renderHeight, isCurrent, isMouseOver);

            // 绘制皮肤图标
            drawSkinIcon(guiGraphics, renderLeft, renderTop, renderHeight, isCurrent);

            // 绘制皮肤信息
            drawSkinInfo(guiGraphics, renderLeft, renderTop, renderWidth, renderHeight, isCurrent, isMouseOver);
        }

        private void drawEntryBackground(GuiGraphics guiGraphics, int left, int top, int width, int height,
                                         boolean isCurrent, boolean isHovered) {
            // 背景颜色
            int backgroundColor;
            if (isCurrent) {
                backgroundColor = SELECTED_COLOR;
            } else if (isHovered) {
                backgroundColor = HOVER_COLOR;
            } else if ((getIndex() % 2) == 0) {
                backgroundColor = 0x20202020; // 交替行颜色
            } else {
                backgroundColor = 0x10202020;
            }

            // 绘制背景
            guiGraphics.fill(left, top, left + width, top + height, backgroundColor);

            // 当前皮肤的边框
            if (isCurrent) {
                int borderColor = 0xFF00FF00;
                guiGraphics.fill(left, top, left + width, top + 2, borderColor);
                guiGraphics.fill(left, top + height - 2, left + width, top + height, borderColor);
                guiGraphics.fill(left, top, left + 2, top + height, borderColor);
                guiGraphics.fill(left + width - 2, top, left + width, top + height, borderColor);
            }

            // 悬停效果
            if (isHovered) {
                int hoverBorderColor = 0x80FFFFFF;
                guiGraphics.fill(left, top, left + width, top + 1, hoverBorderColor);
                guiGraphics.fill(left, top + height - 1, left + width, top + height, hoverBorderColor);
                guiGraphics.fill(left, top, left + 1, top + height, hoverBorderColor);
                guiGraphics.fill(left + width - 1, top, left + width, top + height, hoverBorderColor);
            }
        }

        private void drawSkinIcon(GuiGraphics guiGraphics, int left, int top, int height, boolean isCurrent) {
            int iconSize = height - 12;
            int iconX = left + 8;
            int iconY = top + (height - iconSize) / 2;

            // 绘制图标背景
            int iconBgColor = skinColor;
            if (isCurrent) {
                iconBgColor = blendColors(iconBgColor, 0xFF00FF00, 0.3f);
            }

            // 绘制圆形图标
            int radius = iconSize / 2;
            for (int y = 0; y < iconSize; y++) {
                for (int x = 0; x < iconSize; x++) {
                    float distance = (float) Math.sqrt(Math.pow(x - radius, 2) + Math.pow(y - radius, 2));
                    if (distance <= radius) {
                        int alpha = 0xFF;
                        if (distance > radius - 2) {
                            alpha = (int)((1 - (distance - (radius - 2)) / 2) * 255);
                        }
                        if (alpha > 0) {
                            guiGraphics.fill(iconX + x, iconY + y, iconX + x + 1, iconY + y + 1, (alpha << 24) | (iconBgColor & 0x00FFFFFF));
                        }
                    }
                }
            }

            // 绘制图标边框
            int borderColor = isCurrent ? 0xFF00FF00 : 0x80FFFFFF;
            for (int i = 0; i < 2; i++) {
                int offset = i;
                guiGraphics.fill(iconX - offset, iconY - offset, iconX + iconSize + offset, iconY, borderColor);
                guiGraphics.fill(iconX - offset, iconY + iconSize, iconX + iconSize + offset, iconY + iconSize + offset, borderColor);
                guiGraphics.fill(iconX - offset, iconY, iconX, iconY + iconSize, borderColor);
                guiGraphics.fill(iconX + iconSize, iconY, iconX + iconSize + offset, iconY + iconSize, borderColor);
            }

            // 绘制皮肤首字母
            String initial = skinName.substring(0, 1).toUpperCase();
            if (skinName.equals("default")) {
                initial = "D";
            }

            int textColor = 0xFFFFFFFF;
            int textX = iconX + iconSize / 2 - Minecraft.getInstance().font.width(initial) / 2;
            int textY = iconY + iconSize / 2 - Minecraft.getInstance().font.lineHeight / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, initial, textX, textY, textColor, false);
        }

        private void drawSkinInfo(GuiGraphics guiGraphics, int left, int top, int width, int height,
                                  boolean isCurrent, boolean isHovered) {
            int infoX = left + height; // 图标高度即为偏移量
            int infoY = top + (height - 20) / 2;

            // 皮肤名称
            Component displayName = skinName.equals("default") ?
                    Component.translatable("screen." + TMM.MOD_ID + ".skins.default_skin") :
                    Component.literal(formatSkinName(skinName));

            int nameColor = isCurrent ? 0xFF00FF00 : 0xFFFFFFFF;
            guiGraphics.drawString(Minecraft.getInstance().font, displayName, infoX, infoY, nameColor, false);

            // 皮肤状态
            Component statusText = isCurrent ?
                    Component.translatable("screen." + TMM.MOD_ID + ".skins.equipped") :
                    Component.translatable("screen." + TMM.MOD_ID + ".skins.available");

            int statusColor = isCurrent ? 0xFF00AA00 : 0xFFAAAAAA;
            int statusY = infoY + 12;
            guiGraphics.drawString(Minecraft.getInstance().font, statusText, infoX, statusY, statusColor, false);

            // 装备按钮
            drawEquipButton(guiGraphics, left, top, width, height, isCurrent, isHovered);
        }

        private void drawEquipButton(GuiGraphics guiGraphics, int left, int top, int width, int height,
                                     boolean isCurrent, boolean isHovered) {
            int buttonWidth = 60;
            int buttonHeight = 20;
            int buttonX = left + width - buttonWidth - 10;
            int buttonY = top + (height - buttonHeight) / 2;

            // 按钮背景
            int buttonColor;
            if (isCurrent) {
                buttonColor = 0x8033AA33; // 已装备，绿色
            } else if (isHovered) {
                buttonColor = 0x804488CC; // 悬停，蓝色
            } else {
                buttonColor = 0x80404040; // 正常，灰色
            }

            guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor);

            // 按钮边框
            int borderColor = isCurrent ? 0xFF55FF55 : (isHovered ? 0xFF6688CC : 0xFF555555);
            guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 1, borderColor);
            guiGraphics.fill(buttonX, buttonY + buttonHeight - 1, buttonX + buttonWidth, buttonY + buttonHeight, borderColor);
            guiGraphics.fill(buttonX, buttonY, buttonX + 1, buttonY + buttonHeight, borderColor);
            guiGraphics.fill(buttonX + buttonWidth - 1, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, borderColor);

            // 按钮文字
            Component buttonText = isCurrent ?
                    Component.translatable("screen." + TMM.MOD_ID + ".skins.equipped") :
                    Component.translatable("screen." + TMM.MOD_ID + ".skins.equip");

            int textColor = isCurrent ? 0xFF00FF00 : 0xFFFFFFFF;
            int textX = buttonX + buttonWidth / 2 - Minecraft.getInstance().font.width(buttonText) / 2;
            int textY = buttonY + (buttonHeight - 8) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, buttonText, textX, textY, textColor, false);
        }

        private String formatSkinName(String skinName) {
            // 将下划线或连字符分隔的皮肤名转换为友好格式
            String[] parts = skinName.split("[_\\-]");
            StringBuilder result = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty()) {
                    result.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return result.toString().trim();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) { // 左键点击
                // 获取物品类型名称
                String itemTypeName = getItemTypeName();

                // 更新玩家皮肤组件中物品类型的装备皮肤
                skinsComponent.setEquippedSkinForItemType(itemTypeName, skinName);

                // 更新数据同步
                skinsComponent.setSkinInDataSync(itemStack, skinName);

                parentScreen.refreshSkinPanels();
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("screen." + TMM.MOD_ID + ".skins.narration",
                    skinName.equals("default") ?
                            Component.translatable("screen." + TMM.MOD_ID + ".skins.default_skin") :
                            Component.literal(skinName));
        }

        private int getIndex() {
            return SkinSelectionList.this.children().indexOf(this);
        }
    }

    private static int blendColors(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}