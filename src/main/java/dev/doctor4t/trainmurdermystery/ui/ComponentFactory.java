package dev.doctor4t.trainmurdermystery.ui;

import com.daqem.uilib.api.client.gui.component.event.OnClickEvent;
import com.daqem.uilib.client.gui.component.ButtonComponent;
import com.daqem.uilib.client.gui.component.TextComponent;
import com.daqem.uilib.client.gui.text.Text;
import com.daqem.uilib.client.gui.texture.Textures;
import dev.doctor4t.trainmurdermystery.ui.background.FrostedBackground;
import dev.doctor4t.trainmurdermystery.ui.util.UIStyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;

public class ComponentFactory {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    public static ButtonComponent createButton(String translationKey, OnClickEvent<ButtonComponent> onClick) {
        Font font = Minecraft.getInstance().font;
        MutableComponent text = net.minecraft.network.chat.Component.translatable(translationKey);

        ButtonComponent button = new ButtonComponent(
            Textures.MINECRAFT_BUTTON,
            0, 0, // Position will be set by the layout
            BUTTON_WIDTH, BUTTON_HEIGHT
        );

        ButtonComponent hoverState = new ButtonComponent(
            Textures.MINECRAFT_BUTTON_HOVERED,
            0, 0,
            BUTTON_WIDTH, BUTTON_HEIGHT
        );
        hoverState.setText(new Text(font, text));
        button.setHoverState(hoverState);

        button.setText(new Text(font, text));
        button.setOnClickEvent(onClick);
        return button;
    }

    public static TextComponent createSectionTitle(String translationKey) {
        Font font = Minecraft.getInstance().font;
        MutableComponent text = net.minecraft.network.chat.Component.translatable(translationKey)
            .copy()
            .withStyle(style -> style.withColor(UIStyleHelper.TEXT_COLOR_TITLE).withBold(true));

        return new TextComponent(font, text);
    }
    
    public static FrostedBackground createFrostedBackground(int width, int height) {
        return new FrostedBackground(width, height);
    }
}
