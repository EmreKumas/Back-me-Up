package net.emrekumas.backmeup.addon;

import net.minecraft.src.GameSettings;
import net.minecraft.src.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class BackMeUpKeybind {
    private static final String SAVE_KEY = "backmeup.save";
    private static final String LOAD_KEY = "backmeup.load";
    private static final int DEFAULT_SAVE_KEY = Keyboard.KEY_F6;
    private static final int DEFAULT_LOAD_KEY = Keyboard.KEY_F7;

    public static KeyBinding saveKey;
    public static KeyBinding loadKey;

    public static void initKeybind(GameSettings gameSettings) {
        ArrayList<KeyBinding> keyBindings = new ArrayList<>(List.of(gameSettings.keyBindings));

        keyBindings.forEach(keyBinding -> {
            if (keyBinding.keyDescription.equals(SAVE_KEY)) {
                saveKey = keyBinding;
            } else if (keyBinding.keyDescription.equals(LOAD_KEY)) {
                loadKey = keyBinding;
            }
        });

        if (saveKey == null) {
            saveKey = new KeyBinding(SAVE_KEY, DEFAULT_SAVE_KEY);
            keyBindings.add(saveKey);
            gameSettings.keyBindings = keyBindings.toArray(new KeyBinding[0]);
        }

        if (loadKey == null) {
            loadKey = new KeyBinding(LOAD_KEY, DEFAULT_LOAD_KEY);
            keyBindings.add(loadKey);
            gameSettings.keyBindings = keyBindings.toArray(new KeyBinding[0]);
        }
    }
}
