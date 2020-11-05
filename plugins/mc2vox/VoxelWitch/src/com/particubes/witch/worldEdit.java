package com.particubes.witch;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import ovh.nemesis.cauldron.Voxel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class worldEdit {

    public static List<Voxel> getRegionToVoxel (Player player) {
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().findByName(player.getName());
        Region region;
        try {
            region = localSession.getSelection(localSession.getSelectionWorld());
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
            return null;
        }
        List<Voxel> voxels = new ArrayList<>();
        World world = Witch.instance.getServer().getWorld(region.getWorld().getName());
        JSONObject json = JSONColors.getJSON();
        for (int x = region.getMinimumPoint().getBlockX(); x <= Math.min(region.getMaximumPoint().getBlockX(), region.getMinimumPoint().getBlockX() + 255); x++) {
            for (int y = region.getMinimumPoint().getBlockY(); y <= Math.min(region.getMaximumPoint().getBlockY(), region.getMinimumPoint().getBlockY() + 255); y++) {
                for (int z = region.getMinimumPoint().getBlockZ(); z <= Math.min(region.getMaximumPoint().getBlockZ(), region.getMinimumPoint().getBlockZ() + 255); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!json.getJSONObject("ignored").keySet().contains(block.getType().name().toLowerCase()) && !json.getJSONObject("active").keySet().contains(block.getType().name().toLowerCase())) {
                        continue;
                    }
                    if (json.getJSONObject("ignored").keySet().contains(block.getType().name().toLowerCase())) {
                        if (json.getJSONObject("ignored").getBoolean(block.getType().name().toLowerCase())) {
                            continue;
                        }
                    }
                    String color = json.getJSONObject("active").getString(block.getType().name().toLowerCase());
                    int index = 255;
                    if (json.getJSONObject("colors").keySet().contains(color)) {
                        index = json.getJSONObject("colors").getInt(color);
                    }
                    Voxel voxel = new Voxel((z - region.getMinimumPoint().getBlockZ()), (x - region.getMinimumPoint().getBlockX()), (y - region.getMinimumPoint().getBlockY()), index);
                    voxels.add(voxel);
                }
            }
        }
        System.out.println(voxels.size() + " voxels processed.");
        player.sendMessage("§d" + voxels.size() + " voxels processed.");
        return voxels;
    }
}
