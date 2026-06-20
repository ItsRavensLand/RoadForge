package io.github.ItsRavensLand.roadForge.models;

import org.bukkit.Material;
import java.util.Random;

public enum RoadTier {

    // Base terrain (tier 0)
    GRASS_BLOCK(Material.GRASS_BLOCK, 0),
    DIRT(Material.DIRT, 0),
    COARSE_DIRT(Material.COARSE_DIRT, 0),
    ROOTED_DIRT(Material.ROOTED_DIRT, 0),
    PODZOL(Material.PODZOL, 0),
    MYCELIUM(Material.MYCELIUM, 0),
    SAND(Material.SAND, 0),
    RED_SAND(Material.RED_SAND, 0),
    STONE(Material.STONE, 0),
    DEEPSLATE(Material.DEEPSLATE, 0),
    TUFF(Material.TUFF, 0),
    CALCITE(Material.CALCITE, 0),
    DRIPSTONE_BLOCK(Material.DRIPSTONE_BLOCK, 0),
    SNOW_BLOCK(Material.SNOW_BLOCK, 0),
    POWDER_SNOW(Material.POWDER_SNOW, 0),

    // Road tiers (tier > 0)
    DIRT_PATH(Material.DIRT_PATH, 1),
    GRAVEL(Material.GRAVEL, 2),
    COBBLESTONE(Material.COBBLESTONE, 3),
    MOSSY_COBBLESTONE(Material.MOSSY_COBBLESTONE, 3),
    STONE_BRICKS(Material.STONE_BRICKS, 4),
    MOSSY_STONE_BRICKS(Material.MOSSY_STONE_BRICKS, 4),
    CRACKED_STONE_BRICKS(Material.CRACKED_STONE_BRICKS, 4),
    SMOOTH_STONE(Material.SMOOTH_STONE, 5),
    SMOOTH_BASALT(Material.SMOOTH_BASALT, 5);

    private final Material material;
    private final int tier;
    private static final Random RANDOM = new Random();

    RoadTier(Material material, int tier) {
        this.material = material;
        this.tier     = tier;
    }

    public Material getMaterial()     { return material; }
    public int      getTier()         { return tier; }
    public boolean  isBase()          { return tier == 0; }
    public boolean  isUpgradeable()   { return tier < 5; }

    public static RoadTier fromMaterial(Material m) {
        for (RoadTier rt : values()) {
            if (rt.material == m) return rt;
        }
        return null;
    }

    /** Next tier with biome-aware randomization. */
    public RoadTier next() {
        return switch (this) {
            case GRASS_BLOCK, DIRT, COARSE_DIRT, ROOTED_DIRT, PODZOL, MYCELIUM -> DIRT_PATH;
            case SAND, RED_SAND       -> RANDOM.nextBoolean() ? DIRT_PATH : GRAVEL;
            case STONE, DEEPSLATE, TUFF, CALCITE, DRIPSTONE_BLOCK -> pick(GRAVEL, COBBLESTONE);
            case SNOW_BLOCK, POWDER_SNOW -> GRAVEL;
            case DIRT_PATH            -> GRAVEL;
            case GRAVEL               -> pick(COBBLESTONE, MOSSY_COBBLESTONE);
            case COBBLESTONE, MOSSY_COBBLESTONE -> pick(STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS);
            case STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS -> pick(SMOOTH_STONE, SMOOTH_BASALT);
            case SMOOTH_STONE, SMOOTH_BASALT -> this;
        };
    }

    private static RoadTier pick(RoadTier... options) {
        return options[RANDOM.nextInt(options.length)];
    }
}
