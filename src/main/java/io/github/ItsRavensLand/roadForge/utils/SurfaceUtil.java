package io.github.ItsRavensLand.roadForge.utils;


import io.github.ItsRavensLand.roadForge.models.RoadTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class SurfaceUtil {

    /**
     * Given a location, finds the actual surface block at that X/Z.
     * Returns null if underground or invalid.
     */
    public static Location getSurface(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int highestY = world.getHighestBlockYAt(x, z);

        Location surface = new Location(world, x, highestY, z);
        Material mat = world.getBlockAt(surface).getType();

        // Must be a block RoadTier knows about
        if (RoadTier.fromMaterial(mat) == null) return null;

        // Must have air above (truly on surface)
        Location above = surface.clone().add(0, 1, 0);
        if (!world.getBlockAt(above).getType().isAir()) return null;

        return surface;
    }

    /**
     * Checks if a location is on the surface (not underground).
     */
    public static boolean isOnSurface(Location loc) {
        return getSurface(loc) != null;
    }
}