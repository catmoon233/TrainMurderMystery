package dev.doctor4t.trainmurdermystery.client.gui.screen;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.PlayerSkinsComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public class SkinSelectionPanel extends ContainerObjectSelectionList<SkinSelectionPanel.SkinEntry> {
    private final SkinManagementScreen parentScreen;
    private final ItemStack itemStack;
    private final PlayerSkinsComponent skinsComponent;

    public SkinSelectionPanel(SkinManagementScreen parentScreen, int width, int height, int y0, int y1, int itemHeight, ItemStack itemStack, PlayerSkinsComponent skinsComponent) {
        super(Minecraft.getInstance(), width, height, y0, y1);
        this.parentScreen = parentScreen;
        this.itemStack = itemStack;
        this.skinsComponent = skinsComponent;
        
        // 添加默认皮肤
        this.addEntry(SkinEntry.createDefaultSkin(itemStack, skinsComponent, parentScreen));
        
        // 添加已解锁的皮肤
        Map<String, Boolean> unlockedSkins = skinsComponent.getUnlockedSkins(itemStack);
        for (Map.Entry<String, Boolean> entry : unlockedSkins.entrySet()) {
            if (entry.getValue()) {
                this.addEntry(SkinEntry.createSkinEntry(entry.getKey(), itemStack, skinsComponent, parentScreen));
            }
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    public static class SkinEntry extends ContainerObjectSelectionList.Entry<SkinEntry> {
        private final String skinName;
        private final boolean isCurrentSkin;
        private final Button equipButton;

        public SkinEntry(String skinName, boolean isCurrentSkin, ItemStack itemStack, PlayerSkinsComponent skinsComponent, SkinManagementScreen parentScreen) {
            this.skinName = skinName;
            this.isCurrentSkin = isCurrentSkin;
            
            Component buttonText = isCurrentSkin ? 
                Component.translatable("screen." + TMM.MOD_ID + ".skins.current_skin") : 
                Component.translatable("screen." + TMM.MOD_ID + ".skins.equip_skin");
                
            this.equipButton = Button.builder(buttonText, (button) -> {
                skinsComponent.setEquippedSkin(itemStack, skinName);
                skinsComponent.setSkinInDataSync(itemStack, skinName);
                parentScreen.refreshSkinPanels(); // 刷新所有面板以反映更改
            }).size(60, 20).build();
        }

        public static SkinEntry createDefaultSkin(ItemStack itemStack, PlayerSkinsComponent skinsComponent, SkinManagementScreen parentScreen) {
            return new SkinEntry("default", true, itemStack, skinsComponent, parentScreen); // 假设默认为当前皮肤
        }

        public static SkinEntry createSkinEntry(String skinName, ItemStack itemStack, PlayerSkinsComponent skinsComponent, SkinManagementScreen parentScreen) {
            return new SkinEntry(skinName, false, itemStack, skinsComponent, parentScreen);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            // 绘制背景
            int color = isCurrentSkin ? 0xFF55AA55 : 0xFF404040; // 如果是当前皮肤则使用绿色背景
            guiGraphics.fill(left, top, left + width, top + height, color);
            
            // 绘制皮肤名称
            Component displayName = skinName.equals("default") ? 
                Component.translatable("screen." + TMM.MOD_ID + ".skins.default_skin") : 
                Component.literal(skinName);
                
            guiGraphics.drawString(Minecraft.getInstance().font, displayName.getString(), left + 5, top + height / 2 - 4, 0xFFFFFF);
            
            // 计算按钮位置
            int buttonX = left + width - 70;
            int buttonY = top + (height - 20) / 2;
            
            // 移动按钮到正确位置
            equipButton.setPosition(buttonX, buttonY);
            equipButton.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public boolean mouseClicked(double p_94595_, double p_94596_, int p_94597_) {
            // 当点击条目时也触发装备操作
            if (equipButton.isMouseOver(p_94595_, p_94596_)) {
                return equipButton.mouseClicked(p_94595_, p_94596_, p_94597_);
            }
            return false;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public boolean mouseReleased(double p_94608_, double p_94609_, int p_94610_) {
            return equipButton.mouseReleased(p_94608_, p_94609_, p_94610_);
        }

        @Override
        public boolean mouseDragged(double p_94601_, double p_94602_, int p_94603_, double p_94604_, double p_94605_) {
            return equipButton.mouseDragged(p_94601_, p_94602_, p_94603_, p_94604_, p_94605_);
        }


    }
}