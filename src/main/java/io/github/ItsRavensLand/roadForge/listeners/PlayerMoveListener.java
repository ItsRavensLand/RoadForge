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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final TrafficManager    traffic;
    private final ConfigManager     config;
    private final int               radius;
    private final Map<UUID, long[]> lastPos = new HashMap<>();

    public PlayerMoveListener(RoadForge plugin) {
        this.traffic = plugin.getTrafficManager();
        this.config  = plugin.getConfigManager();
        this.radius  = config.getTrafficRadius();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to   = event.getTo();
        if (to == null || to.getWorld() == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

        Player player = event.getPlayer();
        if (!config.isWorldEnabled(to.getWorld().getName())) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isGliding()) return;

        // Deduplicate: skip if still on same block as last recorded step
        int bx = to.getBlockX(), bz = to.getBlockZ();
        long[] last = lastPos.get(player.getUniqueId());
        if (last != null && last[0] == bx && last[1] == bz) return;
        lastPos.put(player.getUniqueId(), new long[]{bx, bz});

        World world   = to.getWorld();
        int   centerY = to.getBlockY() - 1;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) == radius && Math.abs(dz) == radius) continue; // round corners

                int tx = bx + dx, tz = bz + dz;
                int ty = surfaceY(world, tx, centerY, tz);
                if (ty == Integer.MIN_VALUE) continue;

                Material mat = world.getBlockAt(tx, ty, tz).getType();
                if (RoadTier.fromMaterial(mat) == null) continue;
                if (!world.getBlockAt(tx, ty + 1, tz).getType().isAir()) continue;

                double dist   = Math.sqrt(dx * dx + dz * dz);
                long   points = dist == 0 ? 3 : (dist <= 1 ? 2 : 1);
                traffic.addPoints(new Location(world, tx, ty, tz), points);
            }
        }
    }

    /** Finds walkable surface Y near referenceY (±3 blocks). */
    private int surfaceY(World world, int x, int refY, int z) {
        for (int dy = 0; dy <= 3; dy++) {
            for (int sign : new int[]{0, 1, -1}) {
                int y = refY + (sign * dy);
                if (y < world.getMinHeight() || y >= world.getMaxHeight()) continue;
                if (world.getBlockAt(x, y, z).getType().isSolid()
                        && world.getBlockAt(x, y + 1, z).getType().isAir()) return y;
            }
        }
        return Integer.MIN_VALUE;
    }
}
