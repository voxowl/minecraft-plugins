package com.particubes.witch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ovh.nemesis.cauldron.Color;
import ovh.nemesis.cauldron.Model;
import ovh.nemesis.cauldron.Palette;
import ovh.nemesis.cauldron.exportToVox;

import java.io.FileOutputStream;
import java.io.IOException;

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
            if (args.length == 0) {
                sender.sendMessage("§3Help command :");
                return true;
            }
            if (args[0].equalsIgnoreCase("export")) {
                Model model = new Model();
                model.setVoxels(worldEdit.getRegionToVoxel((Player) sender));

                Palette palette = new Palette();
                palette.setColor(10, new Color(255, 255, 255));

                byte[] bytes = exportToVox.exportToByteArray(model, palette, null);

                try (FileOutputStream fos = new FileOutputStream(Witch.instance.getDataFolder().getPath() + "/test.vox")) {
                    fos.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
