package net.akat.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PlaytimeService {
    long getPlaytimeSeconds(OfflinePlayer player);

    long getRequiredSeconds();

    default boolean hasRequiredPlaytime(Player player) {
        return getPlaytimeSeconds(player) >= getRequiredSeconds();
    }

    default long getRemainingSeconds(Player player) {
        return Math.max(0L, getRequiredSeconds() - getPlaytimeSeconds(player));
    }
}
