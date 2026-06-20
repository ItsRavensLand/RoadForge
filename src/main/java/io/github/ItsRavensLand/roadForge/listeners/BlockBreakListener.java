package io.github.ItsRavensLand.roadForge.listeners;

import io.github.ItsRavensLand.roadForge.models.RoadTier;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Set;

public class BlockBreakListener implements Listener {

    // Road blocks and walls drop nothing when broken
    private static final Set<Material> NO_DROP = Set.of(
            Material.COBBLESTONE_WALL,
            Material.MOSSY_COBBLESTONE_WALL,
            Material.STONE_BRICK_WALL,
            Material.ANDESITE_WALL
    );

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Material mat  = event.getBlock().getType();
        RoadTier tier = RoadTier.fromMaterial(mat);
        if ((tier != null && !tier.isBase()) || NO_DROP.contains(mat)) {
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
    }
}
