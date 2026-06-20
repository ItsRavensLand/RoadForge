package io.github.ItsRavensLand.roadForge.tasks;

import io.github.ItsRavensLand.roadForge.RoadForge;
import io.github.ItsRavensLand.roadForge.models.RoadTier;
import io.github.ItsRavensLand.roadForge.models.TrafficBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.Set;

public class WallTask extends BukkitRunnable {

    private final RoadForge plugin;
    private static final Random     RANDOM  = new Random();
    private static final int[][]    SIDES   = {{1,0},{-1,0},{0,1},{0,-1}};
    private static final Material[] WALLS   = {
            Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL,
            Material.STONE_BRICK_WALL, Material.ANDESITE_WALL
    };

    // All materials considered road (including custom config variants)
    private static final Set<Material> ROAD = Set.of(
            Material.DIRT_PATH, Material.GRAVEL,
            Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
            Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
            Material.SMOOTH_STONE, Material.SMOOTH_BASALT,
            Material.COARSE_DIRT
    );

    public WallTask(RoadForge plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        double chance = plugin.getConfigManager().getWallChance();
        if (chance <= 0) return;

        for (TrafficBlock tb : plugin.getTrafficManager().getAllBlocks()) {
            World world = Bukkit.getWorld(tb.getWorld());
            if (world == null) continue;

            int rx = tb.getX(), ry = tb.getY(), rz = tb.getZ();
            if (!isRoad(world, rx, ry, rz)) continue;

            for (int[] s : SIDES) {
                int sx = rx + s[0], sz = rz + s[1];
                if (isRoad(world, sx, ry, sz)) continue;

                // Only true edge: exactly 1 road neighbor
                int roadNeighbors = 0;
                for (int[] n : SIDES) {
                    if (isRoad(world, sx + n[0], ry, sz + n[1])) roadNeighbors++;
                }
                if (roadNeighbors != 1) continue;

                Location wallLoc = new Location(world, sx, ry + 1, sz);
                if (!world.getBlockAt(wallLoc).getType().isAir()) continue;
                if (!world.getBlockAt(sx, ry, sz).getType().isSolid()) continue;

                if (RANDOM.nextDouble() > chance) continue;
                world.getBlockAt(wallLoc).setType(WALLS[RANDOM.nextInt(WALLS.length)]);
            }
        }
    }

    private boolean isRoad(World w, int x, int y, int z) {
        return ROAD.contains(w.getBlockAt(x, y, z).getType());
    }
}
