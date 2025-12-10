package dev.doctor4t.trainmurdermystery.ui;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Main entry point for TMM Command UI system
 */
public class TMMCommandUI {
    
    public static final String UI_CATEGORY = "key.categories.trainmurdermystery";
    
    public static final KeyBinding OPEN_COMMAND_UI = new KeyBinding(
        "key.trainmurdermystery.open_command_ui",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        UI_CATEGORY
    );
    
    public static void init() {
        KeyBindingHelper.registerKeyBinding(OPEN_COMMAND_UI);
    }
}
