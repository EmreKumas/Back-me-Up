package net.emrekumas.backmeup.controller;

import btw.AddonHandler;
import net.emrekumas.backmeup.records.WorldInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;
import net.minecraft.src.MinecraftException;
import net.minecraft.src.WorldServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.emrekumas.backmeup.constants.Constants.*;

public class BackupController {
    public void update(boolean forceBackup) {
        if (!SingletonController.getConfigurationController().isEnabled()) {
            AddonHandler.logMessage("Backup enabled is false, skipping...");
            return;
        }

        List<WorldInfo> eligibleWorlds = getEligibleWorlds(forceBackup);

        if (eligibleWorlds.isEmpty()) {
            // no need to backup
            return;
        }

        Path backupDirectory = SingletonController.getConfigurationController().getBackupFolder();

        eligibleWorlds.forEach(worldInfo -> {
            Path source = Paths.get(MinecraftServer.getServer().getFile(SAVES).getPath(), worldInfo.worldDirectoryName());
            String worldBackupName = createWorldBackupName(worldInfo);
            Path destination = backupDirectory.resolve(worldBackupName);

            if (Files.exists(destination) || isSameMinute(worldBackupName, forceBackup)) {
                // no need to backup (probably, game pause or very close time since last backup)
                return;
            }

            copyFolder(source, destination);

            SingletonController.getInMemoryControllerInstance().addBackup(worldInfo.worldDirectoryName(), worldBackupName);
            SingletonController.getCleanupController().cleanup(backupDirectory, worldInfo.worldDirectoryName());
        });

        if (SingletonController.getConfigurationController().isNotificationEnabled()) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("Backup completed");
        }
    }

    private boolean isSameMinute(String worldBackupName, boolean forceBackup) {
        if (forceBackup) return false;

        String dayStr = (worldBackupName.split("_Day")[1]).split("_")[0];
        String timeStr = worldBackupName.split("_Day(.*)_")[1];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss");

        Integer day = Integer.parseInt(dayStr);
        LocalTime time = LocalTime.parse(timeStr, dtf);

        String worldName = Minecraft.getMinecraft().getIntegratedServer().getFolderName();
        List<String> backups = SingletonController.getInMemoryControllerInstance().getBackupNames(worldName);

        for (String backup : backups) {
            String backupDayStr = (backup.split("_Day")[1]).split("_")[0];
            String backupTimeStr = backup.split("_Day(.*)_")[1];

            Integer backupDay = Integer.parseInt(backupDayStr);
            LocalTime backupTime = LocalTime.parse(backupTimeStr, dtf);

            if (day.equals(backupDay) && time.getHour() == backupTime.getHour() && time.getMinute() == backupTime.getMinute()) {
                return true;
            }
        }

        return false;
    }

    private static List<WorldInfo> getEligibleWorlds(boolean forceBackup) {
        List<WorldServer> distinctWorlds = Arrays.stream(MinecraftServer.getServer().worldServers)
                .collect(Collectors.toMap(
                        worldServer -> worldServer.getSaveHandler().getWorldDirectoryName(),
                        worldServer -> worldServer,
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .toList();

        return distinctWorlds.stream()
                .filter(worldServer -> forceBackup || isBackupTime(worldServer.getTotalWorldTime()))
                .map(worldServer -> {
                    saveWorldAndPlayerData(worldServer);

                    return new WorldInfo(worldServer.getSaveHandler().getWorldDirectoryName(), worldServer.getTotalWorldTime());
                })
                .toList();
    }

    private static void saveWorldAndPlayerData(WorldServer worldServer) {
        MinecraftServer.getServer().getConfigurationManager().saveAllPlayerData();

        try {
            worldServer.saveAllChunks(true, null);
        } catch (MinecraftException e) {
            throw new RuntimeException("An exception occurred while saving the chunks", e);
        }

        worldServer.flush();
    }

    private static boolean isBackupTime(long totalWorldTime) {
        long dayStartOffset = SingletonController.getConfigurationController().getDayStartOffset();
        long day = (totalWorldTime + dayStartOffset + GAP_TICKS) / ONE_DAY_TICKS + 1;
        long remainingTicks = (totalWorldTime + dayStartOffset + GAP_TICKS) % ONE_DAY_TICKS;
        LocalTime time = convertTicksToTime(remainingTicks);

        String backupFrequencyUnit = SingletonController.getConfigurationController().getBackupFrequencyUnit();
        int backupFrequency = SingletonController.getConfigurationController().getBackupFrequency();

        return switch (backupFrequencyUnit) {
            case "d" -> time.getHour() == 0 && day % backupFrequency == 0;
            case "h" -> time.getHour() % backupFrequency == 0;
            default -> throw new RuntimeException("Backup frequency unit has invalid value");
        };
    }

    private String createWorldBackupName(WorldInfo worldInfo) {
        long dayStartOffset = SingletonController.getConfigurationController().getDayStartOffset();
        long day = (worldInfo.totalWorldTime() + dayStartOffset + GAP_TICKS) / ONE_DAY_TICKS + 1;
        long remainingTicks = (worldInfo.totalWorldTime() + dayStartOffset + GAP_TICKS) % ONE_DAY_TICKS;
        LocalTime time = convertTicksToTime(remainingTicks);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss");

        return worldInfo.worldDirectoryName() + "_Day" + day + "_" + dtf.format(time);
    }

    public static LocalTime convertTicksToTime(long ticks) {
        int totalSeconds = (int) (ticks * 3.6);

        int hours = totalSeconds / 3600;
        int minutes = (int) (((totalSeconds / 3600.0) % 1) * 60);
        int seconds = (int) (((((totalSeconds / 3600.0) % 1) * 60) % 1) * 60);

        // Format into 24-hour time
        return LocalTime.of(hours, minutes, seconds);
    }

    public static void copyFolder(Path source, Path destination) {
        try (Stream<Path> sourcePathStream = Files.walk(source)) {
            sourcePathStream.forEach(sourcePath -> {
                try {
                    Path targetPath = destination.resolve(source.relativize(sourcePath));

                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception exception) {
                    throw new RuntimeException("An error occurred while copying backup folder", exception);
                }
            });
        } catch (Exception exception) {

            throw new RuntimeException("An error occurred while copying backup folder", exception);
        }
    }

    public static List<String> getBackups(Path backupDirectory, String worldDirectoryName, int numberOfBackups) {
        try (Stream<Path> files = Files.list(backupDirectory)) {
            return files
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(folder -> stripDateTime(folder).equals(worldDirectoryName))
                    .sorted(sortNames())
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                Collections.reverse(list);
                                return list;
                            }
                    ))
                    .stream()
                    .limit(numberOfBackups)
                    .toList();
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while traversing backup directory", exception);
        }
    }

    private static Comparator<String> sortNames() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH.mm.ss");

        return (o1, o2) -> {
            String day1Str = (o1.split("_Day")[1]).split("_")[0];
            String day2Str = (o2.split("_Day")[1]).split("_")[0];
            String time1Str = o1.split("_Day(.*)_")[1];
            String time2Str = o2.split("_Day(.*)_")[1];

            LocalTime time1 = LocalTime.parse(time1Str, dtf);
            LocalTime time2 = LocalTime.parse(time2Str, dtf);

            Integer day1 = Integer.parseInt(day1Str);
            Integer day2 = Integer.parseInt(day2Str);

            if (day1.equals(day2)) {
                return time1.compareTo(time2);
            }

            return day1.compareTo(day2);
        };
    }

    public static List<String> getBackupsDateStripped(Path backupDirectory, String worldDirectoryName) {
        try (Stream<Path> files = Files.list(backupDirectory)) {
            return files
                    .filter(Files::isDirectory)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(BackupController::stripDateTime)
                    .filter(folder -> folder.equals(worldDirectoryName))
                    .toList();
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred while traversing backup directory", exception);
        }
    }

    public static String stripDateTime(String wDateTime) {
        return wDateTime.split("_Day")[0];
    }
}
