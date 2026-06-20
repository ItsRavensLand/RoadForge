package io.github.ItsRavensLand.roadForge.commands;
import io.github.ItsRavensLand.roadForge.RoadForge;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class RoadForgeCommand implements CommandExecutor, TabCompleter {

    private final RoadForge plugin;

    public RoadForgeCommand(RoadForge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("roadforge.admin")) {
            sender.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getConfigManager().reload();
                sender.sendMessage("§aRoadForge config reloaded.");
            }
            case "save" -> {
                plugin.getTrafficManager().save();
                sender.sendMessage("§aTraffic data saved.");
            }
            case "info" -> {
                int blocks = plugin.getTrafficManager().getAllBlocks().size();
                sender.sendMessage("§aRoadForge §7| Tracked blocks: §f" + blocks);
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6RoadForge Commands:");
        sender.sendMessage("§e/roadforge reload §7- Reload config");
        sender.sendMessage("§e/roadforge save §7- Force save traffic data");
        sender.sendMessage("§e/roadforge info §7- Show plugin stats");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "save", "info");
        }
        return List.of();
    }
}