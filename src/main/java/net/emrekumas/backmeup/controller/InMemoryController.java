package net.emrekumas.backmeup.controller;

import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryController {

    private final Map<String, List<String>> backups;

    public InMemoryController() {
        backups = new HashMap<>();
    }

    public void initialize() {
        List<String> distinctWorldNames = getDistinctWorldNames();

        distinctWorldNames.forEach(worldName -> {
            Path backupDirectory = getBackupDirectory();

            List<String> backupDirectoryNames = BackupController.getBackups(backupDirectory, worldName, 10);

            backups.put(worldName, new ArrayList<>(backupDirectoryNames));
        });
    }

    private static Path getBackupDirectory() {
        File backupDirectory = SingletonController.getConfigurationController().getBackupFolder().toFile();
        if (!backupDirectory.exists()) {
            boolean created = backupDirectory.mkdirs();

            if (!created) {
                throw new RuntimeException("backups folder cannot be created");
            }
        }

        return backupDirectory.toPath();
    }

    private static List<String> getDistinctWorldNames() {
        return Arrays.stream(MinecraftServer.getServer().worldServers)
                .collect(Collectors.toMap(
                        worldServer -> worldServer.getSaveHandler().getWorldDirectoryName(),
                        worldServer -> worldServer,
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .map(worldServer -> worldServer.getSaveHandler().getWorldDirectoryName())
                .toList();
    }

    public List<String> getBackupNames(String worldDirectoryName) {
        if (!backups.containsKey(worldDirectoryName)) return Collections.emptyList();

        return backups.get(worldDirectoryName);
    }

    public void addBackup(String worldDirectoryName, String worldBackupName) {
        if (!backups.containsKey(worldDirectoryName)) {
            backups.put(worldDirectoryName, new ArrayList<>());
        }

        backups.get(worldDirectoryName).add(0, worldBackupName);
    }

    public void removeBackup(String worldDirectoryName, String worldBackupName) {
        if (!backups.containsKey(worldDirectoryName)) return;

        backups.get(worldDirectoryName).remove(worldBackupName);
    }
}
