package io.github.ItsRavensLand.roadForge.tasks;

import io.github.ItsRavensLand.roadForge.RoadForge;
import io.github.ItsRavensLand.roadForge.models.RoadTier;
import io.github.ItsRavensLand.roadForge.models.TrafficBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WallTask extends BukkitRunnable {

    private final RoadForge plugin;
    private static final Random RANDOM = new Random();
    private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final Material[] WALL_MATERIALS = {
            Material.COBBLESTONE_WALL,
            Material.MOSSY_COBBLESTONE_WALL,
            Material.STONE_BRICK_WALL,
            Material.ANDESITE_WALL
    };

    // All materials that count as "road" for wall placement purposes
    // Includes both RoadTier non-base AND any custom road-block config materials
    private static final Set<Material> ROAD_MATERIALS = new HashSet<>();

    static {
        for (RoadTier tier : RoadTier.values()) {
            if (!tier.isBase()) {
                ROAD_MATERIALS.add(tier.getMaterial());
            }
        }
        // Add all possible custom road-block materials from config tiers
        ROAD_MATERIALS.add(Material.COARSE_DIRT);
        ROAD_MATERIALS.add(Material.DIRT_PATH);
        ROAD_MATERIALS.add(Material.GRAVEL);
        ROAD_MATERIALS.add(Material.COBBLESTONE);
        ROAD_MATERIALS.add(Material.MOSSY_COBBLESTONE);
        ROAD_MATERIALS.add(Material.STONE_BRICKS);
        ROAD_MATERIALS.add(Material.MOSSY_STONE_BRICKS);
        ROAD_MATERIALS.add(Material.CRACKED_STONE_BRICKS);
        ROAD_MATERIALS.add(Material.SMOOTH_STONE);
        ROAD_MATERIALS.add(Material.SMOOTH_BASALT);
    }

    public WallTask(RoadForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        double wallChance = plugin.getConfigManager().getWallChance();
        if (wallChance <= 0) return;

        for (TrafficBlock tb : plugin.getTrafficManager().getAllBlocks()) {
            World world = Bukkit.getWorld(tb.getWorld());
            if (world == null) continue;

            int rx = tb.getX();
            int ry = tb.getY();
            int rz = tb.getZ();

            // Only process actual road blocks
            if (!isRoad(world, rx, ry, rz)) continue;

            for (int[] side : SIDES) {
                int sx = rx + side[0];
                int sz = rz + side[1];

                // Side block must NOT be road
                if (isRoad(world, sx, ry, sz)) continue;

                // Side block must have exactly 1 road neighbor (the road block we came from)
                // More than 1 = it's a gap between roads = interior
                int roadNeighborCount = 0;
                for (int[] n : SIDES) {
                    if (isRoad(world, sx + n[0], ry, sz + n[1])) {
                        roadNeighborCount++;
                    }
                }
                if (roadNeighborCount != 1) continue;

                // Wall goes one block above
                Location wallLoc = new Location(world, sx, ry + 1, sz);
                if (!world.getBlockAt(wallLoc).getType().isAir()) continue;

                // Ground must be solid
                if (!world.getBlockAt(sx, ry, sz).getType().isSolid()) continue;

                if (RANDOM.nextDouble() > wallChance) continue;

                Material wallMat = WALL_MATERIALS[RANDOM.nextInt(WALL_MATERIALS.length)];
                world.getBlockAt(wallLoc).setType(wallMat);
            }
        }
    }

    private boolean isRoad(World world, int x, int y, int z) {
        return ROAD_MATERIALS.contains(world.getBlockAt(x, y, z).getType());
    }
}