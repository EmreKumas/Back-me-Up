package net.emrekumas.backmeup.mixin;

import net.emrekumas.backmeup.controller.SingletonController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(at = @At("TAIL"), method = "runTick()V")
	private void init(CallbackInfo ci) {
		SingletonController.getWorldLoaderControllerInstance().load();

		WorldClient world = Minecraft.getMinecraft().theWorld;
		MinecraftServer server = MinecraftServer.getServer();

		if (world == null || server == null) {
			return;
		}

		if (!isExactHour()) {
			return;
		}

		SingletonController.getBackupController().update(false);
	}

	@Unique
	private boolean isExactHour() {
		long totalWorldTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime();

		return totalWorldTime % 1000 == 0;
	}
}
