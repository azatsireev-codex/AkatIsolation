package net.akat.listeners;

import net.akat.service.BukkitPlaytimeService;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

import java.time.LocalTime;
import java.time.ZoneId;

public class NightLockListener implements Listener {

    private final Plugin plugin;
    private final BukkitPlaytimeService playtimeService;

    public NightLockListener(Plugin plugin, BukkitPlaytimeService playtimeService) {
        this.plugin = plugin;
        this.playtimeService = playtimeService;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getConfig().getBoolean("night_lock.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        if (playtimeService.hasRequiredPlaytime(player)) {
            return;
        }

        String timezone = plugin.getConfig().getString("night_lock.timezone", "Europe/Moscow");
        int startHour = plugin.getConfig().getInt("night_lock.start_hour", 4);
        int endHour = plugin.getConfig().getInt("night_lock.end_hour", 9);

        LocalTime now = LocalTime.now(ZoneId.of(timezone));
        LocalTime start = LocalTime.of(startHour, 0);
        LocalTime end = LocalTime.of(endHour, 0);

        if (!now.isBefore(start) && now.isBefore(end)) {
            String message = ChatColor.of("#f02c4e") + "⛔ Сервер закрыт для новичков с " + startHour + ":00 до " + endHour + ":00 ("
                    + timezone + ").\n"
                    + ChatColor.of("#f35382") + "Вам нужно провести ещё "
                    + ChatColor.of("#f9a4bd") + playtimeService.formatRemainingFor(player)
                    + ChatColor.of("#f35382") + " на сервере выживания.";
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, message);
        }
    }
}
