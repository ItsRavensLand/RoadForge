package io.github.ItsRavensLand.roadForge.tasks;
import io.github.ItsRavensLand.roadForge.RoadForge;
import org.bukkit.scheduler.BukkitRunnable;

public class MergeTask extends BukkitRunnable {

    private final RoadForge plugin;

    public MergeTask(RoadForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getRoadManager().processMerging();
    }
}