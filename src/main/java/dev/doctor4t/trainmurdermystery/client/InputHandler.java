package dev.doctor4t.trainmurdermystery.client;

import dev.doctor4t.trainmurdermystery.cca.MapVotingComponent;
import dev.doctor4t.trainmurdermystery.client.gui.screen.MapSelectorScreen;
import dev.doctor4t.trainmurdermystery.voting.MapVotingManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class InputHandler {
    private static KeyMapping openVotingScreenKeybind;

    public static void initialize() {
        openVotingScreenKeybind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.trainmurdermystery.open_voting_screen",
                GLFW.GLFW_KEY_M,
                "category.trainmurdermystery.general"));

        ClientTickEvents.END_CLIENT_TICK.register(InputHandler::onClientTick);
    }

    public static KeyMapping getOpenVotingScreenKeybind() {
        return openVotingScreenKeybind;
    }

    private static void onClientTick(Minecraft client) {
        if (client == null)
            return;
        if (client.level == null)
            return;
        if (openVotingScreenKeybind.consumeClick()) {
            // 检查是否处于投票阶段
            final MapVotingComponent mapVotingComponent = MapVotingComponent.KEY.get(client.level);
            if (mapVotingComponent.isVotingActive()) {
                // 打开投票界面
                client.setScreen(new MapSelectorScreen());
            }
        }
    }
}