package io.github.ItsRavensLand.roadForge.listeners;

import io.github.ItsRavensLand.roadForge.RoadForge;
import io.github.ItsRavensLand.roadForge.managers.ConfigManager;
import io.github.ItsRavensLand.roadForge.managers.TrafficManager;
import io.github.ItsRavensLand.roadForge.models.RoadTier;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final RoadForge plugin;
    private final TrafficManager trafficManager;
    private final ConfigManager configManager;
    private final int radius;

    public PlayerMoveListener(RoadForge plugin) {
        this.plugin = plugin;
        this.trafficManager = plugin.getTrafficManager();
        this.configManager = plugin.getConfigManager();
        this.radius = configManager.getTrafficRadius();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!hasMoved(event.getFrom(), event.getTo())) return;

        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null || to.getWorld() == null) return;
        if (!configManager.isWorldEnabled(to.getWorld().getName())) return;

        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying()) return;
        if (player.isGliding()) return;

        World world = to.getWorld();
        int centerX = to.getBlockX();
        int centerY = to.getBlockY() - 1;
        int centerZ = to.getBlockZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue;

                int tx = centerX + dx;
                int tz = centerZ + dz;

                int ty = findSurfaceY(world, tx, centerY, tz);
                if (ty == Integer.MIN_VALUE) continue;

                Material mat = world.getBlockAt(tx, ty, tz).getType();
                if (RoadTier.fromMaterial(mat) == null) continue;
                if (!world.getBlockAt(tx, ty + 1, tz).getType().isAir()) continue;

                double dist = Math.sqrt(dx * dx + dz * dz);
                long points = dist == 0 ? 3 : (dist <= 1 ? 2 : 1);

                plugin.getLogger().info("[DEBUG] point -> " + tx + "," + tz);
                trafficManager.addPoints(new Location(world, tx, ty, tz), points);
            }
        }

        plugin.getLogger().info("[DEBUG] Tracked blocks: " + trafficManager.getAllBlocks().size());
    }

    private int findSurfaceY(World world, int x, int referenceY, int z) {
        for (int dy = 0; dy <= 3; dy++) {
            for (int sign : new int[]{0, 1, -1}) {
                int y = referenceY + (sign * dy);
                if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;

                Material mat = world.getBlockAt(x, y, z).getType();
                Material above = world.getBlockAt(x, y + 1, z).getType();

                if (mat.isSolid() && above.isAir()) {
                    return y;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    private boolean hasMoved(Location from, Location to) {
        if (to == null) return false;
        return from.getBlockX() != to.getBlockX()
                || from.getBlockZ() != to.getBlockZ();
    }
}