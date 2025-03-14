package net.emrekumas.backmeup.mixin;

import net.emrekumas.backmeup.addon.BackMeUpKeybind;
import net.emrekumas.backmeup.command.BackMeUpCommand;
import net.emrekumas.backmeup.controller.SingletonController;
import net.minecraft.src.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {
    @Inject(method = "onTick", at = @At("TAIL"))
    private static void onTick(CallbackInfo cbi) {
        if (!Keyboard.getEventKeyState()) {
            return;
        }

        if (BackMeUpKeybind.saveKey.isPressed()) {
            save();
        } else if (BackMeUpKeybind.loadKey.isPressed()) {
            load();
        }
    }

    @Unique
    private static void save() {
        SingletonController.getBackupController().update(true);
    }

    @Unique
    private static void load() {
        BackMeUpCommand.loadCommand(null, "0");
    }
}
