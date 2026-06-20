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
import java.util.stream.Collectors;

public class RoadManager {

    private final ConfigManager      config;
    private final TrafficManager     traffic;
    private final RoadBlockRegistry  blockRegistry;

    private final Set<String> frozenBlocks = new HashSet<>();
    private static final Random RANDOM       = new Random();
    private static final double FREEZE_CHANCE = 0.18;

    public RoadManager(RoadForge plugin) {
        this.config        = plugin.getConfigManager();
        this.traffic       = plugin.getTrafficManager();
        this.blockRegistry = new RoadBlockRegistry(plugin.getConfig());
    }

    public void processUpgrades() {
        List<TrafficBlock> candidates = traffic.getAllBlocks().stream()
                .filter(tb -> tb.getPoints() > 0)
                .sorted((a, b) -> Long.compare(b.getPoints(), a.getPoints()))
                .collect(Collectors.toList());

        for (TrafficBlock tb : candidates) {
            World world = Bukkit.getWorld(tb.getWorld());
            if (world == null || !config.isWorldEnabled(tb.getWorld())) continue;

            Location loc = new Location(world, tb.getX(), tb.getY(), tb.getZ());
            String key   = locKey(loc);

            if (frozenBlocks.contains(key)) continue;

            Material current = world.getBlockAt(loc).getType();
            if (config.isExcluded(current)) continue;

            RoadTier tier = RoadTier.fromMaterial(current);
            if (tier == null || !tier.isUpgradeable()) continue;

            // Surface only
            if (!world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).getType().isAir()) continue;

            RoadTier next      = tier.next();
            long     threshold = getThreshold(next);

            if (tb.getPoints() < threshold) continue;

            double overshoot = (double) tb.getPoints() / threshold;
            double chance    = Math.min(0.9, 0.4 + (overshoot - 1.0) * 0.3);
            if (RANDOM.nextDouble() > chance) continue;

            // Place road block (registry may override material)
            int      tierNum = tierNumber(next);
            Material roadMat = tierNum > 0 ? blockRegistry.pickForTier(tierNum, next.getMaterial()) : next.getMaterial();
            world.getBlockAt(loc).setType(roadMat);

            // Optional overlay
            if (blockRegistry.isOverlayEnabled()) {
                Material overlay = blockRegistry.pickOverlay();
                if (overlay != null) {
                    Location above = loc.clone().add(0, 1, 0);
                    if (world.getBlockAt(above).getType().isAir()) {
                        world.getBlockAt(above).setType(overlay);
                    }
                }
            }

            if (RANDOM.nextDouble() < FREEZE_CHANCE) frozenBlocks.add(key);
        }
    }

    public void processMerging() {
        if (!config.isMergingEnabled()) return;

        List<TrafficBlock> roadBlocks = new ArrayList<>();
        for (TrafficBlock tb : traffic.getAllBlocks()) {
            World world = Bukkit.getWorld(tb.getWorld());
            if (world == null) continue;
            Material mat  = world.getBlockAt(tb.getX(), tb.getY(), tb.getZ()).getType();
            RoadTier tier = RoadTier.fromMaterial(mat);
            if (tier != null && !tier.isBase()) roadBlocks.add(tb);
        }

        int  minDist      = config.getMergeMinDistance();
        int  maxDist      = config.getMergeMaxDistance();
        long pullStrength = config.getMergePullStrength();

        Set<String> processed = new HashSet<>();
        for (int i = 0; i < roadBlocks.size(); i++) {
            for (int j = i + 1; j < roadBlocks.size(); j++) {
                TrafficBlock a = roadBlocks.get(i);
                TrafficBlock b = roadBlocks.get(j);

                if (!a.getWorld().equals(b.getWorld())) continue;

                double dist = dist2D(a, b);
                if (dist < minDist || dist > maxDist) continue;

                String pair = a.getKey() + "|" + b.getKey();
                if (!processed.add(pair)) continue;

                mergePull(a, b, pullStrength);
            }
        }
    }

    private void mergePull(TrafficBlock from, TrafficBlock to, long strength) {
        World world = Bukkit.getWorld(from.getWorld());
        if (world == null) return;

        Location locA = new Location(world, from.getX(), from.getY(), from.getZ());
        Location locB = new Location(world, to.getX(),   to.getY(),   to.getZ());

        List<Location> path = PathFinder.findPath(locA, locB, 25);
        if (path == null || path.size() <= 2) return;

        // Only apply to blocks between endpoints
        for (Location loc : path.subList(1, path.size() - 1)) {
            if (!config.isExcluded(world.getBlockAt(loc).getType())) {
                traffic.addPoints(loc, strength);
            }
        }
    }

    private int tierNumber(RoadTier tier) {
        return switch (tier) {
            case DIRT_PATH                                          -> 1;
            case GRAVEL                                             -> 2;
            case COBBLESTONE, MOSSY_COBBLESTONE                    -> 3;
            case STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS -> 4;
            case SMOOTH_STONE, SMOOTH_BASALT                       -> 5;
            default                                                -> 0;
        };
    }

    private long getThreshold(RoadTier next) {
        return switch (next) {
            case DIRT_PATH                                          -> config.getThresholdForTier("dirt_path");
            case GRAVEL                                             -> config.getThresholdForTier("gravel");
            case COBBLESTONE, MOSSY_COBBLESTONE                    -> config.getThresholdForTier("cobblestone");
            case STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS -> config.getThresholdForTier("stone_bricks");
            case SMOOTH_STONE, SMOOTH_BASALT                       -> config.getThresholdForTier("smooth_stone");
            default                                                -> Long.MAX_VALUE;
        };
    }

    private double dist2D(TrafficBlock a, TrafficBlock b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private String locKey(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public Set<String>       getFrozenBlocks()  { return frozenBlocks; }
    public RoadBlockRegistry getBlockRegistry() { return blockRegistry; }
}
