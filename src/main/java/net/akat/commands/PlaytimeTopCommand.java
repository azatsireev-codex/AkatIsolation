package net.akat.commands;

import net.akat.model.TopEntry;
import net.akat.service.BukkitPlaytimeService;
import net.akat.service.PlaytimeTopService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeTopCommand implements CommandExecutor {

    private final PlaytimeTopService topService;
    private final BukkitPlaytimeService playtimeService;
    private final long cooldownMillis;
    private final Map<UUID, Long> lastUsed = new ConcurrentHashMap<>();

    public PlaytimeTopCommand(PlaytimeTopService topService, BukkitPlaytimeService playtimeService, long cooldownSeconds) {
        this.topService = topService;
        this.playtimeService = playtimeService;
        this.cooldownMillis = Math.max(0L, cooldownSeconds) * 1000L;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду можно использовать только игрокам.");
            return true;
        }

        long now = System.currentTimeMillis();
        Long last = lastUsed.get(player.getUniqueId());
        if (last != null && now - last < cooldownMillis) {
            long waitSeconds = (cooldownMillis - (now - last) + 999L) / 1000L;
            player.sendMessage(ChatColor.RED + "Вы можете использовать команду через " + waitSeconds + " сек.");
            return true;
        }

        List<TopEntry> top = topService.getTopEntries();
        if (top.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Топ ещё формируется, попробуйте через несколько секунд.");
            topService.triggerRefresh();
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "Топ игроков по времени на сервере:");
        int rank = 1;
        for (TopEntry entry : top) {
            player.sendMessage(ChatColor.YELLOW + String.valueOf(rank) + ". " + entry.playerName() + " - "
                    + playtimeService.formatHms(entry.playtimeSeconds()));
            rank++;
        }

        lastUsed.put(player.getUniqueId(), now);
        return true;
    }
}
