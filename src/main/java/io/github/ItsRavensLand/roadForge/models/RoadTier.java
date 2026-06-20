package io.github.ItsRavensLand.roadForge.models;

import org.bukkit.Material;

import java.util.Random;

public enum RoadTier {

    // Grass & soil
    GRASS_BLOCK(Material.GRASS_BLOCK, 0),
    DIRT(Material.DIRT, 0),
    COARSE_DIRT(Material.COARSE_DIRT, 0),
    ROOTED_DIRT(Material.ROOTED_DIRT, 0),
    PODZOL(Material.PODZOL, 0),
    MYCELIUM(Material.MYCELIUM, 0),

    // Desert / beach
    SAND(Material.SAND, 0),
    RED_SAND(Material.RED_SAND, 0),

    // Mountain / stone
    STONE(Material.STONE, 0),
    DEEPSLATE(Material.DEEPSLATE, 0),
    TUFF(Material.TUFF, 0),
    CALCITE(Material.CALCITE, 0),
    DRIPSTONE_BLOCK(Material.DRIPSTONE_BLOCK, 0),

    // Snowy
    SNOW_BLOCK(Material.SNOW_BLOCK, 0),
    POWDER_SNOW(Material.POWDER_SNOW, 0),

    // Road tiers (tier > 0 = already a road)
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
        this.tier = tier;
    }

    public Material getMaterial() { return material; }
    public int getTier() { return tier; }

    public static RoadTier fromMaterial(Material material) {
        for (RoadTier rt : values()) {
            if (rt.material == material) return rt;
        }
        return null;
    }

    /**
     * Returns the next road tier material with some randomization.
     * Stone-type surfaces skip to stone-based roads.
     */
    public RoadTier next() {
        return switch (this) {
            // Soil -> dirt path
            case GRASS_BLOCK, DIRT, COARSE_DIRT, ROOTED_DIRT,
                 PODZOL, MYCELIUM -> DIRT_PATH;

            // Desert/beach -> gravel or dirt path
            case SAND, RED_SAND -> RANDOM.nextBoolean() ? DIRT_PATH : GRAVEL;

            // Stone surfaces skip straight to cobblestone variants
            case STONE, DEEPSLATE, TUFF, CALCITE,
                 DRIPSTONE_BLOCK -> randomPick(GRAVEL, COBBLESTONE);

            // Snow -> gravel (compressed snow)
            case SNOW_BLOCK, POWDER_SNOW -> GRAVEL;

            // Road progression with randomization
            case DIRT_PATH -> GRAVEL;
            case GRAVEL -> randomPick(COBBLESTONE, MOSSY_COBBLESTONE);
            case COBBLESTONE, MOSSY_COBBLESTONE ->
                    randomPick(STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS);
            case STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS ->
                    randomPick(SMOOTH_STONE, SMOOTH_BASALT);

            // Max tier
            case SMOOTH_STONE, SMOOTH_BASALT -> this;
        };
    }

    private static RoadTier randomPick(RoadTier... options) {
        return options[RANDOM.nextInt(options.length)];
    }

    public boolean isUpgradeable() {
        return tier < 5;
    }

    public boolean isBase() {
        return tier == 0;
    }
}