package com.particubes.witch.Events;

import com.particubes.witch.Variables.Pos;
import com.particubes.witch.Witch;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class interactEvent implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.getPlayer().hasPermission("voxelwitch.wand") || Witch.instance.WorldEditAPI // If player doesn't have the permission or if the plugin use WorldEdit's wand
            || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_AIR // If interaction is on air
            || e.getHand() != EquipmentSlot.HAND) { // If interaction is made with main hand
            return;
        }
        if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.valueOf(Witch.instance.getConfig().getString("item-wand").toUpperCase())) {
            e.setCancelled(true);
            Pos nPos;
            if (Witch.instance.posHashMap.containsKey(e.getPlayer())) { // If Hashmap already contains positions
                nPos = Witch.instance.posHashMap.get(e.getPlayer());
            } else {
                nPos = new Pos();
            }

            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (e.getClickedBlock().getX() == nPos.getX1() && e.getClickedBlock().getY() == nPos.getY1() && e.getClickedBlock().getZ() == nPos.getZ1()) { // If the point is already selected, return
                    return;
                }
                nPos.setPos1(e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ(), e.getClickedBlock().getWorld().getName());
                e.getPlayer().sendMessage("§d[VoxelWitch] Position 1 selected : §5" + nPos.getX1() + " " + nPos.getY1() + " " + nPos.getZ1());
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getClickedBlock().getX() == nPos.getX2() && e.getClickedBlock().getY() == nPos.getY2() && e.getClickedBlock().getZ() == nPos.getZ2()) {
                    return;
                }
                nPos.setPos2(e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ(), e.getClickedBlock().getWorld().getName());
                e.getPlayer().sendMessage("§d[VoxelWitch] Position 2 selected : §5" + nPos.getX2() + " " + nPos.getY2() + " " + nPos.getZ2());
            }

            if (Witch.instance.posHashMap.containsKey(e.getPlayer())) {
                Witch.instance.posHashMap.replace(e.getPlayer(), nPos); // Replace old selection
            } else {
                Witch.instance.posHashMap.put(e.getPlayer(), nPos); // Create entry
            }
        }
    }
}
