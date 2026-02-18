package net.akat.commands;

import net.akat.service.PlaytimeTopService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadConfigCommand implements CommandExecutor {

    private static final String PERMISSION = "akatisolation.admin.reload";

    private final Plugin plugin;
    private final PlaytimeTopService topService;

    public ReloadConfigCommand(Plugin plugin, PlaytimeTopService topService) {
        this.plugin = plugin;
        this.topService = topService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
            return true;
        }

        plugin.reloadConfig();
        topService.reloadFromConfig();
        topService.triggerRefresh();

        sender.sendMessage(ChatColor.GREEN + "Конфиг перезагружен. Новые параметры уже применены.");
        return true;
    }
}
