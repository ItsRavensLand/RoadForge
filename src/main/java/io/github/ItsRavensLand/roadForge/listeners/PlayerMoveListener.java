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

    public PlayerMoveListener(RoadForge plugin) {
        this.trafficManager = plugin.getTrafficManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only record when player moves to a new block
        if (!hasMoved(event.getFrom(), event.getTo())) return;

        Location to = event.getTo();
        if (to == null || to.getWorld() == null) return;
        if (!configManager.isWorldEnabled(to.getWorld().getName())) return;

        // Record the block the player is standing on (one below feet)
        Location ground = to.clone();
        ground.setY(to.getBlockY() - 1);

        trafficManager.recordStep(ground);
    }

    private boolean hasMoved(Location from, Location to) {
        if (to == null) return false;
        return from.getBlockX() != to.getBlockX()
                || from.getBlockZ() != to.getBlockZ();
    }
}
