package com.particubes.witch;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import ovh.nemesis.cauldron.Voxel;

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
        for (int x = region.getMinimumPoint().getBlockX(); x < region.getMaximumPoint().getBlockX(); x++) {
            if ((x - region.getMinimumPoint().getBlockX()) > 255) continue;
            for (int y = region.getMinimumPoint().getBlockY(); y < region.getMaximumPoint().getBlockY(); y++) {
                if ((y - region.getMinimumPoint().getBlockY()) > 255) continue;
                for (int z = region.getMinimumPoint().getBlockZ(); z < region.getMaximumPoint().getBlockZ(); z++) {
                    if ((z - region.getMinimumPoint().getBlockZ()) > 255) continue;
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        Voxel voxel = new Voxel((x - region.getMinimumPoint().getBlockX()), (z - region.getMinimumPoint().getBlockZ()), (y - region.getMinimumPoint().getBlockY()), 10);
                        voxels.add(voxel);
                    }
                }
            }
        }
        return voxels;
    }
}
