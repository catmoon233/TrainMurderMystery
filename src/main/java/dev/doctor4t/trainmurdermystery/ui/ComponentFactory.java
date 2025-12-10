package dev.doctor4t.trainmurdermystery.ui;

import com.daqem.uilib.api.client.gui.component.event.OnClickEvent;
import com.daqem.uilib.client.gui.component.ButtonComponent;
import com.daqem.uilib.client.gui.component.TextComponent;
import com.daqem.uilib.client.gui.text.Text;
import com.daqem.uilib.client.gui.texture.Textures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;

public class ComponentFactory {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    public static ButtonComponent createButton(String translationKey, OnClickEvent<ButtonComponent> onClick) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        MutableText text = net.minecraft.text.Text.translatable(translationKey);

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
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        MutableText text = net.minecraft.text.Text.translatable(translationKey)
            .copy()
            .styled(style -> style.withColor(0xFFD700));

        return new TextComponent(font, text);
    }
}
