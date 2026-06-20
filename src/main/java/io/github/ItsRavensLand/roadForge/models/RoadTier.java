package io.github.ItsRavensLand.roadForge.models;

import org.bukkit.Material;

public enum RoadTier {

    GRASS(Material.GRASS_BLOCK, 0),
    DIRT(Material.DIRT, 0),
    COARSE_DIRT(Material.COARSE_DIRT, 0),
    DIRT_PATH(Material.DIRT_PATH, 1),
    GRAVEL(Material.GRAVEL, 2),
    COBBLESTONE(Material.COBBLESTONE, 3),
    STONE_BRICKS(Material.STONE_BRICKS, 4),
    SMOOTH_STONE(Material.SMOOTH_STONE, 5);

    private final Material material;
    private final int tier;

    RoadTier(Material material, int tier) {
        this.material = material;
        this.tier = tier;
    }

    public Material getMaterial() {
        return material;
    }

    public int getTier() {
        return tier;
    }

    public static RoadTier fromMaterial(Material material) {
        for (RoadTier rt : values()) {
            if (rt.material == material) return rt;
        }
        return null;
    }

    public RoadTier next() {
        return switch (this) {
            case GRASS, DIRT, COARSE_DIRT -> DIRT_PATH;
            case DIRT_PATH -> GRAVEL;
            case GRAVEL -> COBBLESTONE;
            case COBBLESTONE -> STONE_BRICKS;
            case STONE_BRICKS -> SMOOTH_STONE;
            case SMOOTH_STONE -> SMOOTH_STONE;
        };
    }

    public boolean isUpgradeable() {
        return this != SMOOTH_STONE;
    }

    public boolean isBase() {
        return this == GRASS || this == DIRT || this == COARSE_DIRT;
    }
}
