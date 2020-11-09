package com.particubes.witch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.nemesis.cauldron.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class commandVoxelWitch implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("voxelwitch")) {
            if (!(sender instanceof Player)) { // Check if sender is Human
                sender.sendMessage("§cYou are not a player !");
                return true;
            }
            if (!sender.hasPermission("voxelwitch.cmd")) { // Check if sender has the permission to access command
                sender.sendMessage("§cYou don't have the permission §4voxelwitch.cmd§c to do that !");
                return true;
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("export")) {
                    if (!sender.hasPermission("voxelwitch.export")) { // Check if sender has the permission to access command
                        sender.sendMessage("§cYou don't have the permission §4voxelwitch.export§c to do that !");
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage : /" + label + " export <filename>");
                        return true;
                    }
                    if (!args[1].matches("[a-zA-Z0-9_]")) {
                        sender.sendMessage("§cFilename must be alphanumeric (a-Z, 0-9, underscores)");
                        return true;
                    }

                    Model model = new Model();
                    
                    List<Voxel> voxels = worldEdit.getRegionToVoxel((Player) sender);
                    if (voxels == null) {
                        return true;
                    }
                    model.setVoxels(voxels);

                    byte[] bytes = exportToVox.exportToByteArray(model, JSONColors.getPalette(), null);

                    File file = new File(Witch.instance.getDataFolder().getPath() + "/voxs/" + args[1] + ".vox");

                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    sender.sendMessage("§dSuccessfully saved into §5" + args[1] + ".vox");
                    return true;
                }
            }
            sender.sendMessage("§8[§dVoxel§5Witch§8] §dHelp command :");
            sender.sendMessage("§8- §d/voxelwitch export <filename> §7Export selection to §dfilename§7.vox file");
            sender.sendMessage("§8- §d/voxelwitch upload <filename> §7Upload selection to §dfilename§7.vox file and get an url link [WIP]");
            return true;
        }
        return false;
    }
}
