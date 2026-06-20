package io.github.ItsRavensLand.roadForge.listeners;


import io.github.ItsRavensLand.roadForge.RoadForge;
import io.github.ItsRavensLand.roadForge.managers.ConfigManager;
import io.github.ItsRavensLand.roadForge.managers.TrafficManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final TrafficManager trafficManager;
    private final ConfigManager configManager;
    private final int radius;

    public PlayerMoveListener(RoadForge plugin) {
        this.trafficManager = plugin.getTrafficManager();
        this.configManager = plugin.getConfigManager();
        this.radius = configManager.getTrafficRadius();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!hasMoved(event.getFrom(), event.getTo())) return;

        Location to = event.getTo();
        if (to == null || to.getWorld() == null) return;
        if (!configManager.isWorldEnabled(to.getWorld().getName())) return;

        // Center block (directly under player)
        Location center = to.clone();
        center.setY(to.getBlockY() - 1);

        // Record center + radius blocks
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Skip corners for a more circular shape
                if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;

                Location candidate = center.clone().add(dx, 0, dz);

                // Only apply on surface
                Location surface = SurfaceUtil.getSurface(candidate);
                if (surface == null) continue;

                // Weight: center gets full point, edges get less
                double dist = Math.sqrt(dx * dx + dz * dz);
                long points = dist == 0 ? 3 : (dist <= 1 ? 2 : 1);

                trafficManager.addPoints(surface, points);
            }
        }
    }

    private boolean hasMoved(Location from, Location to) {
        if (to == null) return false;
        return from.getBlockX() != to.getBlockX()
                || from.getBlockZ() != to.getBlockZ();
    }
}