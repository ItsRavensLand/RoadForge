package io.github.ItsRavensLand.roadForge.managers;


import io.github.ItsRavensLand.roadForge.RoadForge;
import io.github.ItsRavensLand.roadForge.models.RoadTier;
import io.github.ItsRavensLand.roadForge.models.TrafficBlock;
import io.github.ItsRavensLand.roadForge.utils.PathFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

public class RoadManager {

    private final RoadForge plugin;
    private final ConfigManager config;
    private final TrafficManager traffic;

    public RoadManager(RoadForge plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.traffic = plugin.getTrafficManager();
    }

    // Called by UpgradeTask every upgrade-interval
    public void processUpgrades() {
        for (TrafficBlock tb : traffic.getAllBlocks()) {
            World world = Bukkit.getWorld(tb.getWorld());
            if (world == null) continue;
            if (!config.isWorldEnabled(tb.getWorld())) continue;

            Location loc = new Location(world, tb.getX(), tb.getY(), tb.getZ());
            Material current = world.getBlockAt(loc).getType();

            if (config.isExcluded(current)) continue;

            RoadTier tier = RoadTier.fromMaterial(current);
            if (tier == null || !tier.isUpgradeable()) continue;

            RoadTier next = tier.next();
            long threshold = getThresholdForUpgrade(tier, next);

            if (tb.getPoints() >= threshold) {
                world.getBlockAt(loc).setType(next.getMaterial());
            }
        }
    }

    // Called by MergeTask every merge-check-interval
    public void processMerging() {
        if (!config.isMergingEnabled()) return;

        List<TrafficBlock> pathBlocks = new ArrayList<>();
        for (TrafficBlock tb : traffic.getAllBlocks()) {
            World world = Bukkit.getWorld(tb.getWorld());
            if (world == null) continue;
            Location loc = new Location(world, tb.getX(), tb.getY(), tb.getZ());
            Material mat = world.getBlockAt(loc).getType();
            RoadTier tier = RoadTier.fromMaterial(mat);
            if (tier != null && !tier.isBase()) {
                pathBlocks.add(tb);
            }
        }

        int minDist = config.getMergeMinDistance();
        int maxDist = config.getMergeMaxDistance();
        long pullStrength = config.getMergePullStrength();

        // Find pairs of path blocks within merge range
        Set<String> processed = new HashSet<>();
        for (int i = 0; i < pathBlocks.size(); i++) {
            for (int j = i + 1; j < pathBlocks.size(); j++) {
                TrafficBlock a = pathBlocks.get(i);
                TrafficBlock b = pathBlocks.get(j);

                if (!a.getWorld().equals(b.getWorld())) continue;

                double dist = distance2D(a, b);
                if (dist < minDist || dist > maxDist) continue;

                String pairKey = a.getKey() + "|" + b.getKey();
                if (processed.contains(pairKey)) continue;
                processed.add(pairKey);

                applyMergePull(a, b, pullStrength);
            }
        }
    }

    // Uses A* pathfinding to find path between two blocks and add traffic points
    private void applyMergePull(TrafficBlock from, TrafficBlock to, long strength) {
        World world = Bukkit.getWorld(from.getWorld());
        if (world == null) return;

        Location locA = new Location(world, from.getX(), from.getY(), from.getZ());
        Location locB = new Location(world, to.getX(), to.getY(), to.getZ());

        List<Location> path = PathFinder.findPath(locA, locB, 25);
        if (path == null) return;

        for (Location loc : path) {
            Material mat = world.getBlockAt(loc).getType();
            if (!config.isExcluded(mat)) {
                traffic.addPoints(loc, strength);
            }
        }
    }

    private long getThresholdForUpgrade(RoadTier current, RoadTier next) {
        return switch (next) {
            case DIRT_PATH -> config.getThresholdForTier("dirt_path");
            case GRAVEL -> config.getThresholdForTier("gravel");
            case COBBLESTONE -> config.getThresholdForTier("cobblestone");
            case STONE_BRICKS -> config.getThresholdForTier("stone_bricks");
            case SMOOTH_STONE -> config.getThresholdForTier("smooth_stone");
            default -> Long.MAX_VALUE;
        };
    }

    private double distance2D(TrafficBlock a, TrafficBlock b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
}
