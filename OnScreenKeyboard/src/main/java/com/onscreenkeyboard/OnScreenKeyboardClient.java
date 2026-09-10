package com.onscreenkeyboard;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class OnScreenKeyboardClient implements ClientModInitializer {
    public static final String MOD_ID = "onscreenkeyboard";
    public static final KeyBinding OPEN_KEYBOARD = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.onscreenkeyboard.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.onscreenkeyboard")
    );

    @Override
    public void onInitializeClient() {
        // The K key is handled directly by KeyboardMixin so it can be used by
        // Mojo Launcher's custom controls without inserting an unwanted 'k'.
    }

    public static void toggleKeyboard() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof FloatingKeyboardScreen keyboard) {
            keyboard.closeKeyboard();
            return;
        }

        FloatingKeyboardScreen.open(client);
    }
}
