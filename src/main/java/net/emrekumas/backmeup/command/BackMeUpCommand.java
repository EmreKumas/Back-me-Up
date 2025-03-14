package net.emrekumas.backmeup.command;

import btw.AddonHandler;
import net.emrekumas.backmeup.controller.BackupController;
import net.emrekumas.backmeup.controller.SingletonController;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.emrekumas.backmeup.constants.Constants.SAVES;

public class BackMeUpCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "backmeup";
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "/backmeup list OR /backmeup load <index>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return iCommandSender instanceof EntityPlayer;
    }

    @Override
    public void processCommand(ICommandSender iCommandSender, String[] strings) {
        switch (strings.length) {
            case 1 -> {
                if (strings[0].equals("list")) listCommand(iCommandSender);
                else iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("usage: " + getCommandUsage(iCommandSender)));
            }
            case 2 -> {
                if (strings[0].equals("load")) loadCommand(iCommandSender, strings[1]);
                else iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("usage: " + getCommandUsage(iCommandSender)));
            }
            default -> iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("usage: " + getCommandUsage(iCommandSender)));
        }
    }

    private void listCommand(ICommandSender iCommandSender) {
        String worldDirectoryName = Minecraft.getMinecraft().getIntegratedServer().getFolderName();
        List<String> backupNames = SingletonController.getInMemoryControllerInstance().getBackupNames(worldDirectoryName);

        iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Backups:"));
        for (int i = 0; i < backupNames.size(); i++) {
            iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(
                    " " + i + " : " + backupNames.get(i)
            ).setItalic(true));
        }
    }

    public static void loadCommand(ICommandSender iCommandSender, String indexStr) {
        String worldDirectoryName = Minecraft.getMinecraft().getIntegratedServer().getFolderName();
        List<String> backupNames = SingletonController.getInMemoryControllerInstance().getBackupNames(worldDirectoryName);

        int index;
        try {
            index = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("Invalid index");
            return;
        }

        if (index >= backupNames.size()) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage("Invalid index");
            return;
        }

        loadWorld(worldDirectoryName, backupNames.get(index));
    }

    private static void loadWorld(String worldDirectoryName, String worldBackupDirectoryName) {
        Path worldPath = Paths.get(MinecraftServer.getServer().getFile(SAVES).getPath(), worldDirectoryName);

        Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();

        waitFor(1000);

        deleteOldWorld(worldPath);

        waitFor(1000);

        moveBackupDirectoryToSavesFolder(worldPath, worldBackupDirectoryName);

        SingletonController.getWorldLoaderControllerInstance().setWorldToBeLoaded(worldDirectoryName);
    }

    public static void waitFor(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            AddonHandler.logMessage(e.getMessage());
        }
    }

    private static void deleteOldWorld(Path worldPath) {
        try {
            FileUtils.deleteDirectory(worldPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while renaming the old world", e);
        }
    }

    private static void moveBackupDirectoryToSavesFolder(Path worldPath, String worldBackupDirectoryName) {
        Path backupDirectory = SingletonController.getConfigurationController().getBackupFolder();
        Path worldBackupDirectory = Paths.get(backupDirectory.toFile().getPath(), worldBackupDirectoryName);

        BackupController.copyFolder(worldBackupDirectory, worldPath);
    }
}
