package dev.doctor4t.trainmurdermystery.ui.screen;

import com.daqem.uilib.client.gui.AbstractScreen;
import com.daqem.uilib.client.gui.background.Backgrounds;
import com.daqem.uilib.client.gui.component.TextComponent;
import com.daqem.uilib.client.gui.component.io.TextBoxComponent;
import dev.doctor4t.trainmurdermystery.ui.ComponentFactory;
import dev.doctor4t.trainmurdermystery.ui.components.LinearLayoutComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.LinkedList;

public class AdvancedCommandScreen extends AbstractScreen {

    private final Screen parent;
    private TextBoxComponent moneyInput;
    private TextBoxComponent timerInput;

    public AdvancedCommandScreen(Screen parent) {
        super(net.minecraft.text.Text.translatable("tmm.ui.advanced.title"));
        this.parent = parent;
    }

    @Override
    public void startScreen() {
        this.setPauseScreen(false);
        this.setBackground(Backgrounds.getDefaultBackground(this.getWidth(), this.getHeight()));

        // Title
        TextComponent title = new TextComponent(
            this.textRenderer,
            net.minecraft.text.Text.translatable("tmm.ui.advanced.title")
                .copy()
                .styled(style -> style.withBold(true))
        );
        title.centerHorizontally();
        title.setY(15);
        this.addComponent(title);

        LinearLayoutComponent mainLayout = new LinearLayoutComponent(
            this.getWidth() / 2 - 100, 30,
            200, 0,
            LinearLayoutComponent.Orientation.VERTICAL, 10
        );
        this.addComponent(mainLayout);

        // Money Management Section
        mainLayout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.money"));

        LinearLayoutComponent moneyLayout = new LinearLayoutComponent(0, 0, 0, 16, LinearLayoutComponent.Orientation.HORIZONTAL, 5);
        moneyLayout.addChild(new TextComponent(this.textRenderer, net.minecraft.text.Text.translatable("tmm.ui.label.set_money")));
        moneyInput = new TextBoxComponent(new LinkedList<>(), 0, 0, 100, 16, "100");
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

        LinearLayoutComponent timerLayout = new LinearLayoutComponent(0, 0, 0, 16, LinearLayoutComponent.Orientation.HORIZONTAL, 5);
        timerLayout.addChild(new TextComponent(this.textRenderer, net.minecraft.text.Text.translatable("tmm.ui.label.set_timer")));
        timerInput = new TextBoxComponent(new LinkedList<>(), 0, 0, 100, 16, "10");
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
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }));
    }

    private void executeCommand(String command) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.networkHandler.sendCommand(command.replaceFirst("/", ""));
        }
    }

    @Override
    public void onTickScreen(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        // Additional rendering if needed
    }
}
