package com.particubes.witch;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;
import ovh.nemesis.cauldron.Voxel;

import java.util.ArrayList;
import java.util.List;

public class worldEdit {

    public static List<Voxel> getRegionToVoxel (Player player) {
        LocalSession localSession = WorldEdit.getInstance().getSessionManager().findByName(player.getName()); // Get Player WorldEdit Session
        Region region;
        try {
            region = localSession.getSelection(localSession.getSelectionWorld()); // Get player's selection
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
            player.sendMessage("§cPlease make a region selection first.");
            return null;
        }
        List<Voxel> voxels = new ArrayList<>(); // Create new voxel list
        World world = Witch.instance.getServer().getWorld(region.getWorld().getName()); //Get selection world as bukkit world
        JSONObject json = JSONColors.getJSON();
        if ((region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) > 125 || (region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY()) > 125 || (region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) > 125) { // Check if region side is larger than 127 blocks (vox model limit)
            player.sendMessage("§cWarning ! §dRegion side exceeds 126 blocks, the region will be truncated in vox file."); // Show warning to player
        }
        for (int x = region.getMinimumPoint().getBlockX(); x <= Math.min(region.getMaximumPoint().getBlockX(), region.getMinimumPoint().getBlockX() + 125); x++) { // All value of x between min value and max X point (with limit of 126 blocks)
            for (int y = region.getMinimumPoint().getBlockY(); y <= Math.min(region.getMaximumPoint().getBlockY(), region.getMinimumPoint().getBlockY() + 125); y++) { // "
                for (int z = region.getMinimumPoint().getBlockZ(); z <= Math.min(region.getMaximumPoint().getBlockZ(), region.getMinimumPoint().getBlockZ() + 125); z++) { // "
                    Block block = world.getBlockAt(x, y, z); // Get Block with coordinates
                    if (!json.getJSONObject("ignored").keySet().contains(block.getType().name().toLowerCase()) && !json.getJSONObject("active").keySet().contains(block.getType().name().toLowerCase())) { // If spigot material is not in colors.json, continue
                        continue;
                    }
                    if (json.getJSONObject("ignored").keySet().contains(block.getType().name().toLowerCase())) { // If block is not set in ignored part
                        if (json.getJSONObject("ignored").getBoolean(block.getType().name().toLowerCase())) { // If block is set as ignored
                            continue;
                        }
                    }
                    int index = 255; // Default color index
                    try {
                        index = json.getJSONObject("active").getJSONObject(block.getType().name().toLowerCase()).getInt("index"); // Set index color to configured material color index
                    } catch (JSONException ignored) {

                    }
                    Voxel voxel = new Voxel((z - region.getMinimumPoint().getBlockZ()), (x - region.getMinimumPoint().getBlockX()), (y - region.getMinimumPoint().getBlockY()), index); // Create voxel with coordinates (Minecraft/Voxel conversion : Z/X, X/Y, Y/Z)
                    voxels.add(voxel); //Add Voxel to list
                }
            }
        }
        System.out.println(voxels.size() + " voxels processed."); //Show number of voxels in console
        player.sendMessage("§d" + voxels.size() + " voxels processed."); //Show number of voxels to player
        return voxels;
    }
}
