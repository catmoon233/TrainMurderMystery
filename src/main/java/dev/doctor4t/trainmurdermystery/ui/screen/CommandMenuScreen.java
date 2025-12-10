package dev.doctor4t.trainmurdermystery.ui.screen;

import com.daqem.uilib.client.gui.AbstractScreen;
import com.daqem.uilib.client.gui.component.TextComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.ui.ComponentFactory;
import dev.doctor4t.trainmurdermystery.ui.components.LinearLayoutComponent;
import dev.doctor4t.trainmurdermystery.ui.util.UIStyleHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;

/**
 * Main command UI screen for quick access to TMM commands
 */
public class CommandMenuScreen extends AbstractScreen {

    public CommandMenuScreen() {
        super(net.minecraft.text.Text.translatable("tmm.ui.command_menu.title"));
    }

    @Override
    public void startScreen() {
        this.setPauseScreen(false);
        this.setBackground(ComponentFactory.createFrostedBackground(this.getWidth(), this.getHeight()));

        TextComponent title = new TextComponent(
            this.textRenderer,
            net.minecraft.text.Text.translatable("tmm.ui.command_menu.title")
                .copy()
                .styled(style -> style.withBold(true).withColor(UIStyleHelper.TEXT_COLOR_TITLE))
        );
        title.centerHorizontally();
        title.setY(30);
        this.addComponent(title);

        LinearLayoutComponent layout = new LinearLayoutComponent(
            this.getWidth() / 2 - 120, 60,
            240, 0,
            LinearLayoutComponent.Orientation.VERTICAL, UIStyleHelper.LAYOUT_SPACING_SMALL
        );
        this.addComponent(layout);

        // Game Control Section
        layout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.game_control"));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.start_murder", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:start harpymodloader:modded");
            return true;
        }));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.start_discovery", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:start discovery");
            return true;
        }));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.start_loose_ends", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:start loose_ends");
            return true;
        }));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.stop_game", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:stop");
            return true;
        }));

        // Config Section
        layout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.config"));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.show_config", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:config");
            return true;
        }));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.reload_config", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:config reload");
            return true;
        }));

        // Utility Section
        layout.addChild(ComponentFactory.createSectionTitle("tmm.ui.section.utility"));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.update_doors", (component, screen, mouseX, mouseY, button) -> {
            executeCommand("tmm:updatedoors");
            return true;
        }));
        layout.addChild(ComponentFactory.createButton("tmm.ui.button.advanced", (component, screen, mouseX, mouseY, button) -> {
            MinecraftClient.getInstance().setScreen(new AdvancedCommandScreen(CommandMenuScreen.this));
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
        // Game status display
        if (MinecraftClient.getInstance().world != null) {
            World world = MinecraftClient.getInstance().world;
            GameWorldComponent game = GameWorldComponent.KEY.get(world);

            String status = game.isRunning() ? "Running" : "Not Running";
            net.minecraft.text.Text statusText = net.minecraft.text.Text.literal("Game Status: ")
                .append(net.minecraft.text.Text.literal(status)
                    .styled(style -> style.withColor(game.isRunning() ? 0x00FF00 : 0xFF0000)));

            drawContext.drawText(
                this.textRenderer,
                statusText,
                10, 10,
                0xFFFFFF,
                false
            );
        }
    }
}
