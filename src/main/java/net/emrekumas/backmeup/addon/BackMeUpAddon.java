package net.emrekumas.backmeup.addon;

import btw.BTWAddon;
import net.emrekumas.backmeup.command.BackMeUpCommand;
import net.emrekumas.backmeup.controller.SingletonController;

import java.util.Map;

import static net.emrekumas.backmeup.constants.ConfigProperties.*;

public class BackMeUpAddon extends BTWAddon {
    @Override
    public void preInitialize() {
        this.registerProperty(ENABLED.name(), "true", "Enables backups");
        this.registerProperty(NOTIFICATION.name(), "false", "Sends notification when backup is complete");
        this.registerProperty(DAY_START_OFFSET.name(), "6000", "If you're playing with mods, you can enter the offset ticks in which the initial day starts. Leave 6000 if using none.");
        this.registerProperty(BACKUP_FOLDER.name(), null, "Defaults to game_folder/backups");
        this.registerProperty(BACKUP_FREQUENCY_UNIT.name(), "h", "d for day, h for hour (in game time)");
        this.registerProperty(BACKUP_FREQUENCY.name(), "12", "How frequent backups should be made");
        this.registerProperty(CLEANUP_POLICY.name(), "never_clean", "queued or never_clean");
        this.registerProperty(QUEUE_SIZE.name(), "10", "(only for queued cleanup_policy) max number of backups, when exceeds, removes the oldest one");
    }

    @Override
    public void initialize() {
        this.registerAddonCommand(new BackMeUpCommand());
    }

    @Override
    public void handleConfigProperties(Map<String, String> propertyValues) {
        SingletonController.getConfigurationController().loadConfigurations(propertyValues);
    }
}
