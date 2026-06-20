package io.github.ItsRavensLand.roadForge.models;

import org.bukkit.Location;

public class TrafficBlock {

    private final int x;
    private final int y;
    private final int z;
    private final String world;
    private long points;
    private long lastUpdated;

    public TrafficBlock(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
        this.points = 0;
        this.lastUpdated = System.currentTimeMillis();
    }

    public TrafficBlock(int x, int y, int z, String world, long points) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.points = points;
        this.lastUpdated = System.currentTimeMillis();
    }

    public void addPoint() {
        this.points++;
        this.lastUpdated = System.currentTimeMillis();
    }

    public void addPoints(long amount) {
        this.points += amount;
        this.lastUpdated = System.currentTimeMillis();
    }

    public void decay(long amount) {
        this.points = Math.max(0, this.points - amount);
    }

    public String getKey() {
        return world + ":" + x + ":" + y + ":" + z;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public String getWorld() { return world; }
    public long getPoints() { return points; }
    public long getLastUpdated() { return lastUpdated; }
}