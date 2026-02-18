package net.akat.placeholders;

import net.akat.service.BukkitPlaytimeService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PlaytimePlaceholder extends PlaceholderExpansion {

    private final BukkitPlaytimeService playtimeService;

    public PlaytimePlaceholder(BukkitPlaytimeService playtimeService) {
        this.playtimeService = playtimeService;
    }

    @Override
    public String getIdentifier() {
        return "playtime";
    }

    @Override
    public String getAuthor() {
        return "2FORWORD2";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("formatted")) {
            return playtimeService.formatHms(playtimeService.getPlaytimeSeconds(player));
        }

        if (params.equalsIgnoreCase("remaining")) {
            return playtimeService.formatRemainingFor(player);
        }

        return "";
    }
}
