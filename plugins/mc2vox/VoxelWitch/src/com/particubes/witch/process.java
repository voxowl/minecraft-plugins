package com.particubes.witch;

import com.particubes.witch.Variables.Pos;
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

public class process {

    public static List<Voxel> getRegionToVoxel (Player player) {

        Region region;

        Pos pos = Witch.instance.posHashMap.get(player);
        if (!pos.isPos1() || !pos.isPos2()) {
            player.sendMessage("§cPlease make a region selection first.");
            return null;
        }

        List<Voxel> voxels = new ArrayList<>(); // Create new voxel list
        World world = Witch.instance.getServer().getWorld(pos.getWorld()); //Get selection world as bukkit world
        JSONObject json = JSONColors.getJSON();
        if (pos.getDeltaX() > 126 || pos.getDeltaX() > 126 || pos.getDeltaX() > 126) { // Check if region side is larger than 127 blocks (vox model limit)
            player.sendMessage("§cWarning ! §dRegion side exceeds 126 blocks, the region will be truncated in vox file."); // Show warning to player
        }
        for (int x = pos.getMinX(); x <= Math.min(pos.getMaxX(), pos.getMinX() + 125); x++) { // All value of x between min value and max X point (with limit of 126 blocks)
            for (int y = pos.getMinY(); y <= Math.min(pos.getMaxY(), pos.getMinY() + 125); y++) { // "
                for (int z = pos.getMinZ(); z <= Math.min(pos.getMaxZ(), pos.getMinZ() + 125); z++) { // "
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
                    Voxel voxel = new Voxel((z - pos.getMinZ()), (x - pos.getMinX()), (y - pos.getMinY()), index); // Create voxel with coordinates (Minecraft/Voxel conversion : Z/X, X/Y, Y/Z)
                    voxels.add(voxel); //Add Voxel to list
                }
            }
        }
        System.out.println(voxels.size() + " voxels processed."); //Show number of voxels in console
        player.sendMessage("§d" + voxels.size() + " voxels processed."); //Show number of voxels to player
        return voxels;
    }
}
