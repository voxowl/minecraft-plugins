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
                if (args[0].equalsIgnoreCase("export") || args[0].equalsIgnoreCase("upload")) {
                    if (!sender.hasPermission("voxelwitch." + args[0].toLowerCase())) { // Check if sender has the permission to access command
                        sender.sendMessage("§cYou don't have the permission §4voxelwitch." + args[0].toLowerCase() + "§c to do that !");
                        return true;
                    }
                    if (args.length < 2) {
                        sender.sendMessage("§cUsage : /" + label + " " + args[0].toLowerCase() + " <filename>"); // Incomplete command message
                        return true;
                    }
                    if (!args[1].matches("[a-zA-Z0-9_]*")) { // Alphanumeric_ filename check
                        sender.sendMessage("§cFilename must be alphanumeric (a-Z, 0-9, underscores)");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("temp")) { // Check if filename is "temp" to protect upload command
                        sender.sendMessage("§cThe filename §4temp §cis not allowed.");
                        return true;
                    }

                    Model model = new Model(); // New Voxel model
                    
                    List<Voxel> voxels = worldEdit.getRegionToVoxel((Player) sender); // Convert worldedit region to voxel list
                    if (voxels == null) {
                        return true;
                    }
                    model.setVoxels(voxels); // Add voxel list to voxel model

                    byte[] bytes = exportToVox.exportToByteArray(model, JSONColors.getPalette(), JSONColors.getMaterials()); // Convert simple model, with color palette and materials

                    String name = args[0].equalsIgnoreCase("export") ? args[1] : "temp"; // If it's upload command, use "temp" as filename.

                    File file = new File(Witch.instance.getDataFolder().getPath() + "/voxs/" + name + ".vox"); // Create new file with filename

                    if (!file.exists()) {
                        try {
                            boolean ignored = file.createNewFile(); // Create file if not exist
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(bytes); // Write bytes to file
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (args[0].equalsIgnoreCase("export")) {
                        sender.sendMessage("§dSuccessfully saved into §5" + args[1] + ".vox"); // If it's export command, send confirmation message
                    } else {
                        String result = httpRequest.uploadVox(new File(Witch.instance.getDataFolder().getPath() + "/voxs/temp.vox"), args[1]); //Try to upload to VoxelDonjon
                        if (result.equalsIgnoreCase("@UE")) {
                            sender.sendMessage("§cAn unknown error has occured");
                        } else if (result.equalsIgnoreCase("@E1")) {
                            sender.sendMessage("§cThe vox file is larger than 8MiB");
                        } else if (result.equalsIgnoreCase("@E2")) {
                            sender.sendMessage("§cThe file isn't a vox file !");
                        } else {
                            sender.sendMessage("§dYou can download your file at : §5https://" + Witch.instance.getConfig().getString("donjon-hostname") + "/dl/" + result + " §d! §7§o(validity : 1 day)"); //Send link if no error has occurred
                        }
                    }
                    return true;
                }
            }
            sender.sendMessage("§8[§dVoxel§5Witch§8] §dHelp command :"); // Show help if command is not a registered subcommand (or empty subcommand)
            sender.sendMessage("§8- §d/voxelwitch export <filename> §7Export selection to §dfilename§7.vox file");
            sender.sendMessage("§8- §d/voxelwitch upload <filename> §7Upload selection to §dfilename§7.vox file and get an url link");
            return true;
        }
        return false;
    }
}
