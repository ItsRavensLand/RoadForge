package io.github.ItsRavensLand.roadForge;


import io.github.ItsRavensLand.roadForge.commands.RoadForgeCommand;
import io.github.ItsRavensLand.roadForge.listeners.PlayerMoveListener;
import io.github.ItsRavensLand.roadForge.managers.ConfigManager;
import io.github.ItsRavensLand.roadForge.managers.RoadManager;
import io.github.ItsRavensLand.roadForge.managers.TrafficManager;
import io.github.ItsRavensLand.roadForge.tasks.DecayTask;
import io.github.ItsRavensLand.roadForge.tasks.MergeTask;
import io.github.ItsRavensLand.roadForge.tasks.SaveTask;
import io.github.ItsRavensLand.roadForge.tasks.UpgradeTask;
import org.bukkit.plugin.java.JavaPlugin;

public class RoadForge extends JavaPlugin {

    private static RoadForge instance;

    private ConfigManager configManager;
    private TrafficManager trafficManager;
    private RoadManager roadManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.trafficManager = new TrafficManager(this);
        this.roadManager = new RoadManager(this);

        trafficManager.load();

        registerListeners();
        registerCommands();
        startTasks();

        getLogger().info("RoadForge enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (trafficManager != null) {
            trafficManager.save();
        }
        getLogger().info("RoadForge disabled. Traffic data saved.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
    }

    private void registerCommands() {
        var cmd = getCommand("roadforge");
        if (cmd != null) {
            var handler = new RoadForgeCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
    }

    private void startTasks() {
        long saveTicks = configManager.getSaveInterval() * 20L;
        long upgradeTicks = configManager.getUpgradeInterval() * 20L;
        long mergeTicks = configManager.getMergeCheckInterval() * 20L;
        long decayTicks = configManager.getDecayInterval() * 20L;

        new SaveTask(this).runTaskTimerAsynchronously(this, saveTicks, saveTicks);
        new UpgradeTask(this).runTaskTimer(this, upgradeTicks, upgradeTicks);
        new MergeTask(this).runTaskTimer(this, mergeTicks, mergeTicks);

        if (configManager.isDecayEnabled()) {
            new DecayTask(this).runTaskTimerAsynchronously(this, decayTicks, decayTicks);
        }
    }

    public static RoadForge getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TrafficManager getTrafficManager() {
        return trafficManager;
    }

    public RoadManager getRoadManager() {
        return roadManager;
    }
}