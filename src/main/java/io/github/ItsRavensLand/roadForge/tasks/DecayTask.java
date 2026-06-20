package io.github.ItsRavensLand.roadForge.tasks;


import io.github.ItsRavensLand.roadForge.RoadForge;
import org.bukkit.scheduler.BukkitRunnable;

public class DecayTask extends BukkitRunnable {

    private final RoadForge plugin;

    public DecayTask(RoadForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getTrafficManager().applyDecay(
                plugin.getConfigManager().getDecayAmount()
        );
    }
}
