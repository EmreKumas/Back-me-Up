package net.emrekumas.backmeup.mixin;

import net.emrekumas.backmeup.controller.SingletonController;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

	@Unique
	private boolean initialized = false;

	@Inject(at = @At("TAIL"), method = "tick()V")
	private void init(CallbackInfo ci) {
		if (initialized) return;

		if (MinecraftServer.getServer() == null) return;

		SingletonController.getInMemoryControllerInstance().initialize();

		initialized = true;
	}
}
