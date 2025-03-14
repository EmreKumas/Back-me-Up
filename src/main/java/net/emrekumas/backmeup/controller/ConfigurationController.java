package net.emrekumas.backmeup.controller;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.Map;

import static net.emrekumas.backmeup.constants.ConfigProperties.*;

public class ConfigurationController {

    private static final String BACKUPS = "backups";

    private boolean enabled;
    private boolean notificationEnabled;
    private long dayStartOffset;
    private Path backupFolder;
    private String backupFolderString;
    private String backupFrequencyUnit;
    private int backupFrequency;
    private String cleanupPolicy;
    private int queueSize;

    public void loadConfigurations(Map<String, String> propertyValues) {
        enabled = Boolean.parseBoolean(propertyValues.get(ENABLED.name()));
        notificationEnabled = Boolean.parseBoolean(propertyValues.get(NOTIFICATION.name()));
        dayStartOffset = Long.parseLong(propertyValues.get(DAY_START_OFFSET.name()));
        backupFolderString = propertyValues.get(BACKUP_FOLDER.name());
        backupFrequencyUnit = propertyValues.get(BACKUP_FREQUENCY_UNIT.name());
        backupFrequency = Integer.parseInt(propertyValues.get(BACKUP_FREQUENCY.name()));
        cleanupPolicy = propertyValues.get(CLEANUP_POLICY.name());
        queueSize = Integer.parseInt(propertyValues.get(QUEUE_SIZE.name()));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public long getDayStartOffset() {
        return dayStartOffset;
    }

    public Path getBackupFolder() {
        if (backupFolder != null) {
            return backupFolder;
        }

        if (backupFolderString == null || backupFolderString.equals("null")) {
            backupFolder = MinecraftServer.getServer().getFile(BACKUPS).toPath();
        } else {
            backupFolder = Path.of(backupFolderString);
        }

        return backupFolder;
    }

    public String getBackupFrequencyUnit() {
        return backupFrequencyUnit;
    }

    public int getBackupFrequency() {
        return backupFrequency;
    }

    public String getCleanupPolicy() {
        return cleanupPolicy;
    }

    public int getQueueSize() {
        return queueSize;
    }
}
