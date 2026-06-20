package io.github.ItsRavensLand.roadForge.utils;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class PathFinder {

    private record Node(int x, int y, int z, Node parent, double g, double h) {
        double f() { return g + h; }
        String key() { return x + ":" + y + ":" + z; }
    }

    /**
     * Finds a path between two locations using A*.
     * Stays on the surface and avoids water/lava.
     *
     * @param from  start location
     * @param to    end location
     * @param maxNodes max nodes to explore before giving up
     * @return list of locations from start to end (exclusive of start), or null if not found
     */
    public static List<Location> findPath(Location from, Location to, int maxNodes) {
        if (!from.getWorld().equals(to.getWorld())) return null;

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        Map<String, Node> closed = new HashMap<>();

        Node start = new Node(from.getBlockX(), from.getBlockY(), from.getBlockZ(), null, 0, heuristic(from.getBlockX(), from.getBlockZ(), to.getBlockX(), to.getBlockZ()));
        open.add(start);

        int explored = 0;
        while (!open.isEmpty() && explored < maxNodes) {
            Node current = open.poll();
            explored++;

            if (current.x == to.getBlockX() && current.z == to.getBlockZ()) {
                return reconstructPath(current, from.getWorld());
            }

            String key = current.key();
            if (closed.containsKey(key)) continue;
            closed.put(key, current);

            for (int[] dir : DIRECTIONS) {
                int nx = current.x + dir[0];
                int nz = current.z + dir[1];
                int ny = getSurfaceY(from.getWorld(), nx, nz, current.y);

                if (ny < 0) continue;

                Location neighborLoc = new Location(from.getWorld(), nx, ny, nz);
                Material mat = from.getWorld().getBlockAt(neighborLoc).getType();
                if (isBlocked(mat)) continue;

                String nKey = nx + ":" + ny + ":" + nz;
                if (closed.containsKey(nKey)) continue;

                double newG = current.g + 1.0 + (Math.abs(ny - current.y) * 0.5);
                double newH = heuristic(nx, nz, to.getBlockX(), to.getBlockZ());
                open.add(new Node(nx, ny, nz, current, newG, newH));
            }
        }

        return null;
    }

    private static List<Location> reconstructPath(Node end, org.bukkit.World world) {
        LinkedList<Location> path = new LinkedList<>();
        Node current = end;
        while (current.parent != null) {
            path.addFirst(new Location(world, current.x, current.y, current.z));
            current = current.parent;
        }
        return path;
    }

    private static int getSurfaceY(org.bukkit.World world, int x, int z, int nearY) {
        // Search near current Y for walkable surface
        for (int dy = 2; dy >= -2; dy--) {
            int y = nearY + dy;
            if (y < 0 || y > world.getMaxHeight()) continue;
            Location loc = new Location(world, x, y, z);
            Location below = new Location(world, x, y - 1, z);
            Material mat = world.getBlockAt(loc).getType();
            Material belowMat = world.getBlockAt(below).getType();
            if (mat.isAir() && belowMat.isSolid() && !isBlocked(belowMat)) {
                return y;
            }
        }
        return -1;
    }

    private static boolean isBlocked(Material mat) {
        return mat == Material.WATER || mat == Material.LAVA
                || mat == Material.BEDROCK || !mat.isSolid();
    }

    private static double heuristic(int x1, int z1, int x2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(z1 - z2); // Manhattan
    }

    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };
}
