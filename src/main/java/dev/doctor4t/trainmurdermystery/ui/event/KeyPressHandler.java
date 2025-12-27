package dev.doctor4t.trainmurdermystery.ui.event;

import dev.doctor4t.trainmurdermystery.ui.TMMCommandUI;
import dev.doctor4t.trainmurdermystery.ui.screen.CommandMenuScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

/**
 * Handles key press events for opening the command UI
 */
public class KeyPressHandler {
    
    private static boolean wasKeyDown = false;
    
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            boolean isKeyDown = TMMCommandUI.OPEN_COMMAND_UI.isDown();
            
            if (isKeyDown && !wasKeyDown) {
                // Key just pressed
                openCommandUI();
            }
            
            wasKeyDown = isKeyDown;
        });
    }
    
    private static void openCommandUI() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.hasPermissions(2)) {
            mc.setScreen(new CommandMenuScreen());
        }
    }
}
