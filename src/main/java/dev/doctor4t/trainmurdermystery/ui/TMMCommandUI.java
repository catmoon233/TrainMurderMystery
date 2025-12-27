package dev.doctor4t.trainmurdermystery.ui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Main entry point for TMM Command UI system
 */
public class TMMCommandUI {
    
    public static final String UI_CATEGORY = "key.categories.trainmurdermystery";
    
    public static final KeyMapping OPEN_COMMAND_UI = new KeyMapping(
        "key.trainmurdermystery.open_command_ui",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        UI_CATEGORY
    );
    
    public static void init() {
        KeyBindingHelper.registerKeyBinding(OPEN_COMMAND_UI);
    }
}
