package io.github.ItsRavensLand.roadForge.tasks;


import io.github.ItsRavensLand.roadForge.RoadForge;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {

    private final RoadForge plugin;

    public SaveTask(RoadForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getTrafficManager().save();
    }
}
