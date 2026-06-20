package io.github.ItsRavensLand.roadForge.managers;


import io.github.ItsRavensLand.roadForge.RoadForge;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class ConfigManager {

    private final RoadForge plugin;
    private FileConfiguration config;

    // Thresholds
    private long thresholdDirtPath;
    private long thresholdGravel;
    private long thresholdCobblestone;
    private long thresholdStoneBricks;
    private long thresholdSmoothStone;

    // Intervals
    private int saveInterval;
    private int upgradeInterval;
    private int mergeCheckInterval;
    private int decayInterval;

    // Decay
    private boolean decayEnabled;
    private long decayAmount;

    // Merging
    private boolean mergingEnabled;
    private int mergeMinDistance;
    private int mergeMaxDistance;
    private long mergePullStrength;

    // Traffic radius
    private int trafficRadius;

    // Worlds & blocks
    private Set<String> enabledWorlds;
    private Set<Material> excludedBlocks;
    private Set<Material> upgradeableBlocks;

    public ConfigManager(RoadForge plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        thresholdDirtPath = config.getLong("thresholds.dirt_path", 50);
        thresholdGravel = config.getLong("thresholds.gravel", 200);
        thresholdCobblestone = config.getLong("thresholds.cobblestone", 500);
        thresholdStoneBricks = config.getLong("thresholds.stone_bricks", 1000);
        thresholdSmoothStone = config.getLong("thresholds.smooth_stone", 2000);

        saveInterval = config.getInt("save-interval", 300);
        upgradeInterval = config.getInt("upgrade-interval", 60);
        mergeCheckInterval = config.getInt("merge-check-interval", 120);

        decayEnabled = config.getBoolean("decay.enabled", true);
        decayInterval = config.getInt("decay.interval", 3600);
        decayAmount = config.getLong("decay.amount", 5);

        trafficRadius = config.getInt("traffic-radius", 2);

        mergingEnabled = config.getBoolean("merging.enabled", true);
        mergeMinDistance = config.getInt("merging.min-distance", 10);
        mergeMaxDistance = config.getInt("merging.max-distance", 20);
        mergePullStrength = config.getLong("merging.pull-strength", 5);

        enabledWorlds = new HashSet<>(config.getStringList("enabled-worlds"));

        excludedBlocks = new HashSet<>();
        for (String s : config.getStringList("excluded-blocks")) {
            try { excludedBlocks.add(Material.valueOf(s)); } catch (Exception ignored) {}
        }

        upgradeableBlocks = new HashSet<>();
        for (String s : config.getStringList("upgradeable-blocks")) {
            try { upgradeableBlocks.add(Material.valueOf(s)); } catch (Exception ignored) {}
        }
    }

    public long getThresholdForTier(String tier) {
        return switch (tier) {
            case "dirt_path" -> thresholdDirtPath;
            case "gravel" -> thresholdGravel;
            case "cobblestone" -> thresholdCobblestone;
            case "stone_bricks" -> thresholdStoneBricks;
            case "smooth_stone" -> thresholdSmoothStone;
            default -> Long.MAX_VALUE;
        };
    }

    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.isEmpty() || enabledWorlds.contains(worldName);
    }

    public boolean isExcluded(Material material) {
        return excludedBlocks.contains(material);
    }

    public boolean isUpgradeable(Material material) {
        return upgradeableBlocks.contains(material);
    }

    public int getTrafficRadius() { return trafficRadius; }
    public int getSaveInterval() { return saveInterval; }
    public int getUpgradeInterval() { return upgradeInterval; }
    public int getMergeCheckInterval() { return mergeCheckInterval; }
    public int getDecayInterval() { return decayInterval; }
    public boolean isDecayEnabled() { return decayEnabled; }
    public long getDecayAmount() { return decayAmount; }
    public boolean isMergingEnabled() { return mergingEnabled; }
    public int getMergeMinDistance() { return mergeMinDistance; }
    public int getMergeMaxDistance() { return mergeMaxDistance; }
    public long getMergePullStrength() { return mergePullStrength; }
    public Set<String> getEnabledWorlds() { return enabledWorlds; }
}