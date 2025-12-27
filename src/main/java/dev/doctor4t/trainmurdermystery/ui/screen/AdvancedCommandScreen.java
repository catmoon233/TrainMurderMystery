package dev.doctor4t.trainmurdermystery.ui.screen;

import com.daqem.uilib.client.gui.AbstractScreen;
import com.daqem.uilib.client.gui.component.TextComponent;
import com.daqem.uilib.client.gui.component.io.TextBoxComponent;
import dev.doctor4t.trainmurdermystery.ui.ComponentFactory;
import dev.doctor4t.trainmurdermystery.ui.components.LinearLayoutComponent;
import dev.doctor4t.trainmurdermystery.ui.util.UIStyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class AdvancedCommandScreen extends AbstractScreen {

    private final Screen parent;
    private TextBoxComponent moneyInput;
    private TextBoxComponent timerInput;

    public AdvancedCommandScreen(Screen parent) {
        super(net.minecraft.network.chat.Component.translatable("tmm.ui.advanced.title"));
        this.parent = parent;
    }

    @Override
    public void startScreen() {
        this.setPauseScreen(false);
        this.setBackground(ComponentFactory.createFrostedBackground(this.getWidth(), this.getHeight()));

        TextComponent title = new TextComponent(
            this.font,
            net.minecraft.network.chat.Component.translatable("tmm.ui.advanced.title")
                .copy()
                .withStyle(style -> style.withBold(true).withColor(UIStyleHelper.TEXT_COLOR_TITLE))
        );
        title.centerHorizontally();
        title.setY(25);
        this.addComponent(title);

        LinearLayoutComponent mainLayout = new LinearLayoutComponent(
            this.getWidth() / 2 - UIStyleHelper.LAYOUT_PADDING_HORIZONTAL, 50,
            240, 0,
            LinearLayoutComponent.Orientation.VERTICAL, UIStyleHelper.LAYOUT_SPACING_MEDIUM
        );
        this.addComponent(mainLayout);

        // Money Management Section
        mainLayout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.money"));

        LinearLayoutComponent moneyLayout = new LinearLayoutComponent(0, 0, 0, UIStyleHelper.INPUT_HEIGHT, LinearLayoutComponent.Orientation.HORIZONTAL, UIStyleHelper.LAYOUT_SPACING_SMALL);
        TextComponent moneyLabel = new TextComponent(this.font, 
            net.minecraft.network.chat.Component.translatable("tmm.ui.label.set_money")
                .withStyle(style -> style.withColor(UIStyleHelper.TEXT_COLOR_LABEL)));
        moneyLayout.addChild(moneyLabel);
        moneyInput = new TextBoxComponent(0, 0, UIStyleHelper.INPUT_WIDTH, UIStyleHelper.INPUT_HEIGHT, "100");
        moneyLayout.addChild(moneyInput);
        mainLayout.addChild(moneyLayout);

        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.set_money", (component, screen, mouseX, mouseY, button) -> {
            String amount = moneyInput.getValue();
            if (!amount.isEmpty() && amount.matches("\\d+")) {
                executeCommand("tmm:setmoney @s " + amount);
            }
            return true;
        }));

        // Timer Section
        mainLayout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.timer"));

        LinearLayoutComponent timerLayout = new LinearLayoutComponent(0, 0, 0, UIStyleHelper.INPUT_HEIGHT, LinearLayoutComponent.Orientation.HORIZONTAL, UIStyleHelper.LAYOUT_SPACING_SMALL);
        TextComponent timerLabel = new TextComponent(this.font, 
            net.minecraft.network.chat.Component.translatable("tmm.ui.label.set_timer")
                .withStyle(style -> style.withColor(UIStyleHelper.TEXT_COLOR_LABEL)));
        timerLayout.addChild(timerLabel);
        timerInput = new TextBoxComponent(0, 0, UIStyleHelper.INPUT_WIDTH, UIStyleHelper.INPUT_HEIGHT, "10");
        timerLayout.addChild(timerInput);
        mainLayout.addChild(timerLayout);

        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.set_timer", (component, screen, mouseX, mouseY, button) -> {
            String minutes = timerInput.getValue();
            if (!minutes.isEmpty() && minutes.matches("\\d+")) {
                executeCommand("tmm:settimer " + minutes);
            }
            return true;
        }));

        // Role Management Section
        mainLayout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.role_management"));
        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.reset_weights", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:resetweights");
            return true;
        }));
        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.check_weights", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:checkweights");
            return true;
        }));

        // Auto Start Section
        mainLayout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.autostart"));
        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.enable_autostart", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:autostart enable");
            return true;
        }));
        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.disable_autostart", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:autostart disable");
            return true;
        }));

        // Back button
        mainLayout.addChild(ComponentFactory.createButton("tmm.ui.button.back", (component, screen, mouseX, mouseY, button) -> {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }));
    }

    private void executeCommand(String command) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.connection.sendUnsignedCommand(command.replaceFirst("/", ""));
        }
    }

    @Override
    public void onTickScreen(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        // Additional rendering if needed
    }
}
