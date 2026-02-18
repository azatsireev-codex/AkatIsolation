package net.akat.service;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BukkitPlaytimeService implements PlaytimeService {

    private final Plugin plugin;

    public BukkitPlaytimeService(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public long getPlaytimeSeconds(org.bukkit.OfflinePlayer player) {
        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return ticks / 20L;
    }

    @Override
    public long getRequiredSeconds() {
        long requiredMinutes = plugin.getConfig().getLong("required_playtime_minutes", 3000L);
        return requiredMinutes * 60L;
    }

    public String formatHms(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String formatRemainingFor(Player player) {
        return formatHms(getRemainingSeconds(player));
    }
}
