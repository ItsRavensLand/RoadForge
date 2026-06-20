package io.github.ItsRavensLand.roadForge.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Loads road-blocks and overlay-blocks from config.
 * Uses weighted random selection.
 */
public class RoadBlockRegistry {

    // tier (1-5) -> weighted list of materials
    private final Map<Integer, List<WeightedMaterial>> tierBlocks = new HashMap<>();

    // overlay weighted list (null entry = NONE/skip)
    private final List<WeightedMaterial> overlayBlocks = new ArrayList<>();
    private boolean overlayEnabled = false;

    private static final Random RANDOM = new Random();

    public RoadBlockRegistry(FileConfiguration config) {
        load(config);
    }

    public void load(FileConfiguration config) {
        tierBlocks.clear();
        overlayBlocks.clear();

        // Load road-blocks per tier
        ConfigurationSection roadSection = config.getConfigurationSection("road-blocks");
        if (roadSection != null) {
            for (int tier = 1; tier <= 5; tier++) {
                ConfigurationSection tierSection = roadSection.getConfigurationSection("tier-" + tier);
                if (tierSection == null) continue;

                List<WeightedMaterial> list = new ArrayList<>();
                for (String key : tierSection.getKeys(false)) {
                    if (key.equals("NONE")) {
                        list.add(new WeightedMaterial(null, tierSection.getInt(key)));
                        continue;
                    }
                    try {
                        Material mat = Material.valueOf(key);
                        list.add(new WeightedMaterial(mat, tierSection.getInt(key)));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!list.isEmpty()) tierBlocks.put(tier, list);
            }
        }

        // Load overlay blocks
        overlayEnabled = config.getBoolean("overlay.enabled", false);
        ConfigurationSection overlaySection = config.getConfigurationSection("overlay.blocks");
        if (overlaySection != null) {
            for (String key : overlaySection.getKeys(false)) {
                if (key.equals("NONE")) {
                    overlayBlocks.add(new WeightedMaterial(null, overlaySection.getInt(key)));
                    continue;
                }
                try {
                    Material mat = Material.valueOf(key);
                    overlayBlocks.add(new WeightedMaterial(mat, overlaySection.getInt(key)));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    /**
     * Returns a random material for the given tier (1-5).
     * Falls back to RoadTier default if not configured.
     */
    public Material pickForTier(int tier, Material fallback) {
        List<WeightedMaterial> list = tierBlocks.get(tier);
        if (list == null || list.isEmpty()) return fallback;
        WeightedMaterial picked = weightedPick(list);
        return picked.material != null ? picked.material : fallback;
    }

    /**
     * Returns a random overlay material, or null if NONE or disabled.
     */
    public Material pickOverlay() {
        if (!overlayEnabled || overlayBlocks.isEmpty()) return null;
        WeightedMaterial picked = weightedPick(overlayBlocks);
        return picked.material; // null = NONE = skip
    }

    public boolean isOverlayEnabled() {
        return overlayEnabled;
    }

    private WeightedMaterial weightedPick(List<WeightedMaterial> list) {
        int total = list.stream().mapToInt(w -> w.weight).sum();
        int roll = RANDOM.nextInt(Math.max(total, 1));
        int cumulative = 0;
        for (WeightedMaterial wm : list) {
            cumulative += wm.weight;
            if (roll < cumulative) return wm;
        }
        return list.get(list.size() - 1);
    }

    private record WeightedMaterial(Material material, int weight) {}
}
