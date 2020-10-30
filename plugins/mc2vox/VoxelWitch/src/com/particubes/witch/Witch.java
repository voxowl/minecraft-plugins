package com.particubes.witch;

import org.bukkit.plugin.java.JavaPlugin;

public class Witch extends JavaPlugin {

    public static Witch instance;
    boolean WorldEditAPI = false;

    @Override
    public void onEnable() {
        instance = this;
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            getLogger().info("WorldEdit found.");
            WorldEditAPI = true;
        } else {
            getLogger().info("WorldEdit not found. Disabling VoxelWitch...");
            getServer().getPluginManager().disablePlugin(this);
        }
        getLogger().info("VoxelWitch is now active ! Check out our Voxel Game : https://particubes.com ! :)");
    }

    @Override
    public void onDisable() {

    }
}
