package io.github.ItsRavensLand.roadForge.managers;

import io.github.ItsRavensLand.roadForge.RoadForge;
import io.github.ItsRavensLand.roadForge.models.TrafficBlock;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class TrafficManager {

    private final RoadForge plugin;
    private final Map<String, TrafficBlock> data = new ConcurrentHashMap<>();
    private final File dataFile;

    public TrafficManager(RoadForge plugin) {
        this.plugin   = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "traffic.yml");
    }

    public void addPoints(Location location, long amount) {
        String key = key(location);
        data.computeIfAbsent(key, k -> new TrafficBlock(location)).addPoints(amount);
    }

    public long getPoints(Location location) {
        TrafficBlock block = data.get(key(location));
        return block == null ? 0 : block.getPoints();
    }

    public Collection<TrafficBlock> getAllBlocks() {
        return data.values();
    }

    public void applyDecay(long amount) {
        data.values().forEach(b -> b.decay(amount));
        data.entrySet().removeIf(e -> e.getValue().getPoints() == 0);
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (TrafficBlock b : data.values()) {
            String k = b.getKey().replace(".", "_");
            yaml.set("blocks." + k + ".x",      b.getX());
            yaml.set("blocks." + k + ".y",      b.getY());
            yaml.set("blocks." + k + ".z",      b.getZ());
            yaml.set("blocks." + k + ".world",  b.getWorld());
            yaml.set("blocks." + k + ".points", b.getPoints());
        }
        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save traffic data", e);
        }
    }

    public void load() {
        if (!dataFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        var section = yaml.getConfigurationSection("blocks");
        if (section == null) return;

        for (String k : section.getKeys(false)) {
            int    x      = section.getInt(k + ".x");
            int    y      = section.getInt(k + ".y");
            int    z      = section.getInt(k + ".z");
            String world  = section.getString(k + ".world", "world");
            long   points = section.getLong(k + ".points");
            TrafficBlock block = new TrafficBlock(x, y, z, world, points);
            data.put(block.getKey(), block);
        }
        plugin.getLogger().info("Loaded " + data.size() + " traffic blocks.");
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }
}
