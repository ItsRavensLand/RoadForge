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
    private final Map<String, TrafficBlock> trafficData = new ConcurrentHashMap<>();
    private File dataFile;

    public TrafficManager(RoadForge plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "traffic.yml");
    }

    public void recordStep(Location location) {
        String key = buildKey(location);
        trafficData.computeIfAbsent(key, k -> new TrafficBlock(location)).addPoint();
    }

    public void addPoints(Location location, long amount) {
        String key = buildKey(location);
        trafficData.computeIfAbsent(key, k -> new TrafficBlock(location)).addPoints(amount);
    }

    public long getPoints(Location location) {
        TrafficBlock block = trafficData.get(buildKey(location));
        return block == null ? 0 : block.getPoints();
    }

    public TrafficBlock getBlock(Location location) {
        return trafficData.get(buildKey(location));
    }

    public Collection<TrafficBlock> getAllBlocks() {
        return trafficData.values();
    }

    public void applyDecay(long amount) {
        trafficData.values().forEach(block -> block.decay(amount));
        trafficData.entrySet().removeIf(e -> e.getValue().getPoints() == 0);
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (TrafficBlock block : trafficData.values()) {
            String key = block.getKey().replace(".", "_");
            yaml.set("blocks." + key + ".x", block.getX());
            yaml.set("blocks." + key + ".y", block.getY());
            yaml.set("blocks." + key + ".z", block.getZ());
            yaml.set("blocks." + key + ".world", block.getWorld());
            yaml.set("blocks." + key + ".points", block.getPoints());
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

        for (String key : section.getKeys(false)) {
            int x = section.getInt(key + ".x");
            int y = section.getInt(key + ".y");
            int z = section.getInt(key + ".z");
            String world = section.getString(key + ".world", "world");
            long points = section.getLong(key + ".points");
            TrafficBlock block = new TrafficBlock(x, y, z, world, points);
            trafficData.put(block.getKey(), block);
        }
        plugin.getLogger().info("Loaded " + trafficData.size() + " traffic blocks.");
    }

    private String buildKey(Location location) {
        return location.getWorld().getName() + ":"
                + location.getBlockX() + ":"
                + location.getBlockY() + ":"
                + location.getBlockZ();
    }
}