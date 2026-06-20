package io.github.ItsRavensLand.roadForge;

import io.github.ItsRavensLand.roadForge.commands.RoadForgeCommand;
import io.github.ItsRavensLand.roadForge.listeners.BlockBreakListener;
import io.github.ItsRavensLand.roadForge.listeners.PlayerMoveListener;
import io.github.ItsRavensLand.roadForge.managers.ConfigManager;
import io.github.ItsRavensLand.roadForge.managers.RoadManager;
import io.github.ItsRavensLand.roadForge.managers.TrafficManager;
import io.github.ItsRavensLand.roadForge.tasks.*;
import org.bukkit.plugin.java.JavaPlugin;

public class RoadForge extends JavaPlugin {

    private static RoadForge instance;

    private ConfigManager  configManager;
    private TrafficManager trafficManager;
    private RoadManager    roadManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager  = new ConfigManager(this);
        trafficManager = new TrafficManager(this);
        roadManager    = new RoadManager(this);
        trafficManager.load();

        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);

        var cmd = getCommand("roadforge");
        if (cmd != null) {
            var handler = new RoadForgeCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        scheduleTasks();
        getLogger().info("RoadForge is active.");
    }

    @Override
    public void onDisable() {
        if (trafficManager != null) trafficManager.save();
        getLogger().info("RoadForge stopped. Data saved.");
    }

    private void scheduleTasks() {
        long save    = configManager.getSaveInterval()       * 20L;
        long upgrade = configManager.getUpgradeInterval()    * 20L;
        long merge   = configManager.getMergeCheckInterval() * 20L;
        long decay   = configManager.getDecayInterval()      * 20L;

        new SaveTask(this).runTaskTimerAsynchronously(this, save, save);
        new UpgradeTask(this).runTaskTimer(this, upgrade, upgrade);
        new MergeTask(this).runTaskTimer(this, merge, merge);
        new WallTask(this).runTaskTimer(this, upgrade * 3, upgrade * 3);

        if (configManager.isDecayEnabled()) {
            new DecayTask(this).runTaskTimerAsynchronously(this, decay, decay);
        }
    }

    public static RoadForge  getInstance()         { return instance; }
    public ConfigManager     getConfigManager()    { return configManager; }
    public TrafficManager    getTrafficManager()   { return trafficManager; }
    public RoadManager       getRoadManager()      { return roadManager; }
}
