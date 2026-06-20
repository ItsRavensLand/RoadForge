package io.github.ItsRavensLand.roadForge.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

/** A* pathfinder for road merging. Stays on surface, avoids water/lava. */
public class PathFinder {

    private record Node(int x, int y, int z, Node parent, double g, double h) {
        double f()      { return g + h; }
        String key()    { return x + ":" + y + ":" + z; }
    }

    private static final int[][] DIRS = {
            {1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}
    };

    /**
     * Finds a surface path between two locations.
     * @return locations from start to end (endpoints excluded), or null if unreachable
     */
    public static List<Location> findPath(Location from, Location to, int maxNodes) {
        if (!from.getWorld().equals(to.getWorld())) return null;

        World world = from.getWorld();
        PriorityQueue<Node>  open   = new PriorityQueue<>(Comparator.comparingDouble(Node::f));
        Map<String, Node>    closed = new HashMap<>();

        open.add(new Node(from.getBlockX(), from.getBlockY(), from.getBlockZ(), null,
                0, h(from.getBlockX(), from.getBlockZ(), to.getBlockX(), to.getBlockZ())));

        int explored = 0;
        while (!open.isEmpty() && explored++ < maxNodes) {
            Node cur = open.poll();

            if (cur.x == to.getBlockX() && cur.z == to.getBlockZ()) {
                return buildPath(cur, world);
            }

            if (!closed.putIfAbsent(cur.key(), cur).equals(cur)) continue;

            for (int[] d : DIRS) {
                int nx = cur.x + d[0], nz = cur.z + d[1];
                int ny = surfaceY(world, nx, nz, cur.y);
                if (ny < 0) continue;

                Material mat = world.getBlockAt(nx, ny, nz).getType();
                if (blocked(mat)) continue;

                String nk = nx + ":" + ny + ":" + nz;
                if (closed.containsKey(nk)) continue;

                double ng = cur.g + 1.0 + Math.abs(ny - cur.y) * 0.5;
                double nh = h(nx, nz, to.getBlockX(), to.getBlockZ());
                open.add(new Node(nx, ny, nz, cur, ng, nh));
            }
        }
        return null;
    }

    private static List<Location> buildPath(Node end, World world) {
        LinkedList<Location> path = new LinkedList<>();
        for (Node n = end; n.parent != null; n = n.parent) {
            path.addFirst(new Location(world, n.x, n.y, n.z));
        }
        return path;
    }

    private static int surfaceY(World world, int x, int z, int nearY) {
        for (int dy = 2; dy >= -2; dy--) {
            int y = nearY + dy;
            if (y < 0 || y >= world.getMaxHeight()) continue;
            if (world.getBlockAt(x, y, z).getType().isAir()
                    && world.getBlockAt(x, y - 1, z).getType().isSolid()
                    && !blocked(world.getBlockAt(x, y - 1, z).getType())) return y;
        }
        return -1;
    }

    private static boolean blocked(Material m) {
        return m == Material.WATER || m == Material.LAVA || m == Material.BEDROCK || !m.isSolid();
    }

    private static double h(int x1, int z1, int x2, int z2) {
        return Math.abs(x1 - x2) + Math.abs(z1 - z2);
    }
}
