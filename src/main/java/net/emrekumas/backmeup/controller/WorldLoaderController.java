package net.emrekumas.backmeup.controller;

import net.minecraft.src.Minecraft;

import static net.emrekumas.backmeup.command.BackMeUpCommand.waitFor;

public class WorldLoaderController {

    private String worldToBeLoaded;

    public void load() {
        if (worldToBeLoaded == null) {
            return;
        }

        Minecraft.getMinecraft().loadWorld(null);

        waitFor(500);

        Minecraft.getMinecraft().launchIntegratedServer(worldToBeLoaded, worldToBeLoaded, null);

        worldToBeLoaded = null;
    }

    public void setWorldToBeLoaded(String worldToBeLoaded) {
        this.worldToBeLoaded = worldToBeLoaded;
    }
}
