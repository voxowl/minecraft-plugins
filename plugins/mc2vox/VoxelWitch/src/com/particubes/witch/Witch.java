package com.particubes.witch;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Objects;

public class Witch extends JavaPlugin {

    public static Witch instance;
    boolean WorldEditAPI = false;

    @Override
    public void onEnable() {
        instance = this; //Set instance
        File file = new File(getDataFolder() + "/voxs"); //Vox files folder
        if (!file.exists()) { //If folder doesn't exist, create it
            boolean result = file.mkdir();
            if (!result) { //if mkdir failed, disable the plugin
                getLogger().info("Can't create the folder VoxelWitch/voxs ! Disabling VoxelWitch...");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
        file = new File(getDataFolder() + "/colors.json"); //colors.json config file
        if (!file.exists()) { // If the config file doesn't exist in config directory
            try {
                InputStream inputStream = getResource("colors.json"); // read content of jar's resource
                if (inputStream == null) {
                    getLogger().info("Error when trying to read colors.json in jar file. Disabling VoxelWitch...");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                byte[] buffer = new byte[inputStream.available()];
                int ignored = inputStream.read(buffer);
                OutputStream outputStream = new FileOutputStream(file);
                outputStream.write(buffer); // write the content of resource into the new config file
            } catch (IOException  | NullPointerException e) {
                e.printStackTrace(); // If there is an error, disable plugin
                getLogger().info("File copy colors.json failed. Disabling VoxelWitch...");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        Objects.requireNonNull(this.getCommand("voxelwitch")).setExecutor(new commandVoxelWitch()); //Register command
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) { // Check if WorldEdit is loaded
            getLogger().info("WorldEdit found.");
            WorldEditAPI = true;
        } else {
            getLogger().info("WorldEdit not found. Disabling VoxelWitch..."); // If WorldEdit is not found, disable VoxelWitch
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("VoxelWitch is now active ! Check out Particubes Voxel Game : https://particubes.com ! :)"); // Little ad because ... I love Particubes
    }

    @Override
    public void onDisable() {

    }
}
