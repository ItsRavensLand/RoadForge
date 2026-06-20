package io.github.ItsRavensLand.roadForge.tasks;

import io.github.ItsRavensLand.roadForge.RoadForge;
import org.bukkit.scheduler.BukkitRunnable;

public class UpgradeTask extends BukkitRunnable {

    private final RoadForge plugin;

    public UpgradeTask(RoadForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getRoadManager().processUpgrades();
    }
}