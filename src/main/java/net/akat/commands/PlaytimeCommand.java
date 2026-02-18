package net.akat.commands;

import net.akat.service.BukkitPlaytimeService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand implements CommandExecutor {

    private final BukkitPlaytimeService playtimeService;

    public PlaytimeCommand(BukkitPlaytimeService playtimeService) {
        this.playtimeService = playtimeService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду можно использовать только игрокам.");
            return true;
        }

        long playtimeSeconds = playtimeService.getPlaytimeSeconds(player);
        String current = playtimeService.formatHms(playtimeSeconds);

        player.sendMessage(ChatColor.YELLOW + "⏳ " + ChatColor.of("#e8ee1c") + "Ваше время на сервере: "
                + ChatColor.AQUA + current + ChatColor.of("#fbf4ce") + " (час:мин:сек)");

        if (!playtimeService.hasRequiredPlaytime(player)) {
            player.sendMessage(ChatColor.RED + "⏰ " + ChatColor.of("#e8ee1c") + "Есть ограничение по времени: "
                    + ChatColor.GREEN + "ДА " + ChatColor.of("#e8ee1c") + "(Нужно ещё "
                    + playtimeService.formatRemainingFor(player) + " для полного доступа).");
        } else {
            player.sendMessage(ChatColor.GREEN + "✅ " + ChatColor.of("#e8ee1c") + "Есть ограничение по времени: "
                    + ChatColor.RED + "НЕТ " + ChatColor.of("#e8ee1c") + "(Ограничений нет).");
        }

        return true;
    }
}
