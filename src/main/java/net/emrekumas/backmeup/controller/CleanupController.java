package net.emrekumas.backmeup.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class CleanupController {

    private static final String NEVER_CLEAN = "never_clean";
    private static final String QUEUED = "queued";

    public void cleanup(Path backupDirectory, String worldDirectoryName) {
        String cleanupPolicy = SingletonController.getConfigurationController().getCleanupPolicy();

        if (cleanupPolicy.equals(NEVER_CLEAN)) {
            return;
        }

        long numberOfBackups = getNumberOfBackups(backupDirectory, worldDirectoryName);
        int queueSize = SingletonController.getConfigurationController().getQueueSize();

        if (numberOfBackups <= queueSize) {
            return;
        }

        do {
            Optional<Path> oldestDirectoryOpt = findOldestDirectory(backupDirectory, worldDirectoryName);

            if (oldestDirectoryOpt.isEmpty()) {
                throw new RuntimeException("Error occurred while cleanup process");
            }

            Path oldestDirectory = oldestDirectoryOpt.get();
            deleteDirectory(oldestDirectory);
            numberOfBackups--;

            SingletonController.getInMemoryControllerInstance().removeBackup(worldDirectoryName, oldestDirectory.getFileName().toString());
        } while (numberOfBackups > queueSize);
    }

    private static long getNumberOfBackups(Path backupDirectory, String worldDirectoryName) {
        return BackupController.getBackupsDateStripped(backupDirectory, worldDirectoryName).size();
    }

    public static Optional<Path> findOldestDirectory(Path parentDirectory, String worldDirectoryName) {
        try (Stream<Path> paths = Files.list(parentDirectory)) {
            return paths
                    .filter(Files::isDirectory)
                    .filter(folder -> BackupController.stripDateTime(folder.getFileName().toString()).equals(worldDirectoryName))
                    .min(Comparator.comparing(CleanupController::getCreationTime));
        } catch (IOException e) {
            throw new RuntimeException("Failed to find the oldest directory", e);
        }
    }

    private static FileTime getCreationTime(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            throw new RuntimeException("Unable to read creation time", e);
        }
    }

    public static void deleteDirectory(Path directory) {
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
        } catch (Exception exception) {
            throw new RuntimeException("Couldn't delete oldest backup folder", exception);
        }
    }
}
