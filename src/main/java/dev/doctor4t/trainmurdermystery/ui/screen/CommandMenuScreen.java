package dev.doctor4t.trainmurdermystery.ui.screen;

import com.daqem.uilib.client.gui.AbstractScreen;
import com.daqem.uilib.client.gui.component.TextComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.ui.ComponentFactory;
import dev.doctor4t.trainmurdermystery.ui.components.LinearLayoutComponent;
import dev.doctor4t.trainmurdermystery.ui.util.UIStyleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.Level;

/**
 * Main command UI screen for quick access to TMM commands
 */
public class CommandMenuScreen extends AbstractScreen {

    public CommandMenuScreen() {
        super(net.minecraft.network.chat.Component.translatable("tmm.ui.command_menu.title"));
    }

    @Override
    public void startScreen() {
        this.setPauseScreen(false);
        this.setBackground(ComponentFactory.createFrostedBackground(this.getWidth(), this.getHeight()));

        TextComponent title = new TextComponent(
            this.font,
            net.minecraft.network.chat.Component.translatable("tmm.ui.command_menu.title")
                .copy()
                .withStyle(style -> style.withBold(true).withColor(UIStyleHelper.TEXT_COLOR_TITLE))
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
            Minecraft.getInstance().setScreen(new AdvancedCommandScreen(CommandMenuScreen.this));
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
        // Game status display
        if (Minecraft.getInstance().level != null) {
            Level world = Minecraft.getInstance().level;
            GameWorldComponent game = GameWorldComponent.KEY.get(world);

            String status = game.isRunning() ? "Running" : "Not Running";
            net.minecraft.network.chat.Component statusText = net.minecraft.network.chat.Component.literal("Game Status: ")
                .append(net.minecraft.network.chat.Component.literal(status)
                    .withStyle(style -> style.withColor(game.isRunning() ? 0x00FF00 : 0xFF0000)));

            drawContext.drawString(
                this.font,
                statusText,
                10, 10,
                0xFFFFFF,
                false
            );
        }
    }
}
