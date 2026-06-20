package io.github.ItsRavensLand.roadForge.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/** Weighted random selection of road and overlay blocks from config. */
public class RoadBlockRegistry {

    private final Map<Integer, List<WeightedMaterial>> tierBlocks    = new HashMap<>();
    private final List<WeightedMaterial>               overlayBlocks = new ArrayList<>();
    private       boolean                              overlayEnabled = false;

    private static final Random RANDOM = new Random();

    public RoadBlockRegistry(FileConfiguration config) {
        load(config);
    }

    public void load(FileConfiguration config) {
        tierBlocks.clear();
        overlayBlocks.clear();

        ConfigurationSection roadSection = config.getConfigurationSection("road-blocks");
        if (roadSection != null) {
            for (int tier = 1; tier <= 5; tier++) {
                ConfigurationSection sec = roadSection.getConfigurationSection("tier-" + tier);
                if (sec == null) continue;
                List<WeightedMaterial> list = new ArrayList<>();
                for (String key : sec.getKeys(false)) {
                    if (key.equals("NONE")) { list.add(new WeightedMaterial(null, sec.getInt(key))); continue; }
                    try { list.add(new WeightedMaterial(Material.valueOf(key), sec.getInt(key))); }
                    catch (IllegalArgumentException ignored) {}
                }
                if (!list.isEmpty()) tierBlocks.put(tier, list);
            }
        }

        overlayEnabled = config.getBoolean("overlay.enabled", false);
        ConfigurationSection overlaySec = config.getConfigurationSection("overlay.blocks");
        if (overlaySec != null) {
            for (String key : overlaySec.getKeys(false)) {
                if (key.equals("NONE")) { overlayBlocks.add(new WeightedMaterial(null, overlaySec.getInt(key))); continue; }
                try { overlayBlocks.add(new WeightedMaterial(Material.valueOf(key), overlaySec.getInt(key))); }
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    /** Pick road material for a tier; falls back to default if tier not configured. */
    public Material pickForTier(int tier, Material fallback) {
        List<WeightedMaterial> list = tierBlocks.get(tier);
        if (list == null || list.isEmpty()) return fallback;
        WeightedMaterial picked = weightedPick(list);
        return picked.material != null ? picked.material : fallback;
    }

    /** Pick overlay material; returns null if disabled or NONE is rolled. */
    public Material pickOverlay() {
        if (!overlayEnabled || overlayBlocks.isEmpty()) return null;
        return weightedPick(overlayBlocks).material;
    }

    public boolean isOverlayEnabled() { return overlayEnabled; }

    private WeightedMaterial weightedPick(List<WeightedMaterial> list) {
        int total = list.stream().mapToInt(w -> w.weight).sum();
        int roll  = RANDOM.nextInt(Math.max(total, 1));
        int cum   = 0;
        for (WeightedMaterial wm : list) {
            cum += wm.weight;
            if (roll < cum) return wm;
        }
        return list.get(list.size() - 1);
    }

    private record WeightedMaterial(Material material, int weight) {}
}
