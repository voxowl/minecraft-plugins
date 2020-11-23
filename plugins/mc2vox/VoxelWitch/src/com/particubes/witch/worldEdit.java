package com.particubes.witch;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.json.JSONException;
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
            player.sendMessage("§cPlease make a region selection first.");
            return null;
        }
        List<Voxel> voxels = new ArrayList<>();
        World world = Witch.instance.getServer().getWorld(region.getWorld().getName());
        JSONObject json = JSONColors.getJSON();
        if ((region.getMaximumPoint().getBlockX() - region.getMinimumPoint().getBlockX()) > 125 || (region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY()) > 125 || (region.getMaximumPoint().getBlockZ() - region.getMinimumPoint().getBlockZ()) > 125) {
            player.sendMessage("§cWarning ! §dRegion side exceeds 126 blocks, the region will be truncated in vox file.");
        }
        for (int x = region.getMinimumPoint().getBlockX(); x <= Math.min(region.getMaximumPoint().getBlockX(), region.getMinimumPoint().getBlockX() + 125); x++) {
            for (int y = region.getMinimumPoint().getBlockY(); y <= Math.min(region.getMaximumPoint().getBlockY(), region.getMinimumPoint().getBlockY() + 125); y++) {
                for (int z = region.getMinimumPoint().getBlockZ(); z <= Math.min(region.getMaximumPoint().getBlockZ(), region.getMinimumPoint().getBlockZ() + 125); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (!json.getJSONObject("ignored").keySet().contains(block.getType().name().toLowerCase()) && !json.getJSONObject("active").keySet().contains(block.getType().name().toLowerCase())) {
                        continue;
                    }
                    if (json.getJSONObject("ignored").keySet().contains(block.getType().name().toLowerCase())) {
                        if (json.getJSONObject("ignored").getBoolean(block.getType().name().toLowerCase())) {
                            continue;
                        }
                    }
                    int index = 255;
                    try {
                        index = json.getJSONObject("active").getJSONObject(block.getType().name().toLowerCase()).getInt("index");
                    } catch (JSONException ignored) {

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
