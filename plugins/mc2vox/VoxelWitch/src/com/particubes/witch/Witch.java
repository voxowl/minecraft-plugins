package com.particubes.witch;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Witch extends JavaPlugin {

    public static Witch instance;
    boolean WorldEditAPI = false;

    @Override
    public void onEnable() {
        instance = this; //Set instance
        File file = new File(getDataFolder() + "/voxs"); //Vox files folder
        if (!file.exists()) { //If folder doesn't exist, create it
            file.mkdir();
        }
        this.getCommand("voxelwitch").setExecutor(new commandVoxelWitch()); //Register command
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) { // Check if WorldEdit is loaded
            getLogger().info("WorldEdit found.");
            WorldEditAPI = true;
        } else {
            getLogger().info("WorldEdit not found. Disabling VoxelWitch..."); // If WorldEdit is not found, disable VoxelWitch
            getServer().getPluginManager().disablePlugin(this);
        }
        getLogger().info("VoxelWitch is now active ! Check out our Voxel Game : https://particubes.com ! :)"); // Little ad because ... I love Particubes
    }

    @Override
    public void onDisable() {

    }
}
