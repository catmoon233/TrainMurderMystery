package dev.doctor4t.trainmurdermystery.ui.background;

import com.daqem.uilib.client.gui.background.AbstractBackground;
import dev.doctor4t.trainmurdermystery.ui.util.UIStyleHelper;
import net.minecraft.client.gui.GuiGraphics;

public class FrostedBackground extends AbstractBackground<FrostedBackground> {

    public FrostedBackground(int width, int height) {
        super(width, height);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 渲染半透明黑色背景 (ARGB: 0x40000000 = 25% opacity black)
        context.fill(0, 0, this.getWidth(), this.getHeight(), 0x40000000);
        
        // 在顶部渲染一个半透明的渐变，模拟磨砂玻璃效果
        context.fillGradient(0, 0, this.getWidth(), this.getHeight(), 
            UIStyleHelper.BACKGROUND_COLOR_TOP, 
            UIStyleHelper.BACKGROUND_COLOR_BOTTOM);
    }
}
