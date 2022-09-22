package com.particubes.witch;

import com.particubes.witch.Events.interactEvent;
import com.particubes.witch.Variables.Pos;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

public class Witch extends JavaPlugin {

    public static Witch instance;
    public boolean WorldEditAPI = false;
    public HashMap<Player, Pos> posHashMap = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Save config.yml
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
        Objects.requireNonNull(this.getCommand("voxelwitch")).setTabCompleter(new commandVoxelWitch()); //Register autocomplete
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null && getConfig().getBoolean("worldedit")) { // Check if WorldEdit is loaded and activated in config file
            getLogger().info("WorldEdit found.");
            WorldEditAPI = true;
        } else {
            getLogger().info("WorldEdit not found."); // If WorldEdit is not found
            getServer().getPluginManager().registerEvents(new interactEvent(), this);
            return;
        }
        getLogger().info("VoxelWitch is now active ! Check out Cubzh Voxel Game : https://cu.bzh ! :)");
    }

    @Override
    public void onDisable() {

    }
}
