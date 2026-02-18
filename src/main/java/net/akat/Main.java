package net.akat;

import net.akat.commands.PlaytimeCommand;
import net.akat.commands.PlaytimeTopCommand;
import net.akat.commands.ReloadConfigCommand;
import net.akat.listeners.NightLockListener;
import net.akat.listeners.RestrictionListener;
import net.akat.placeholders.PlaytimePlaceholder;
import net.akat.service.BukkitPlaytimeService;
import net.akat.service.CachedPlaytimeTopService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private BukkitPlaytimeService playtimeService;
    private CachedPlaytimeTopService topService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playtimeService = new BukkitPlaytimeService(this);
        this.topService = new CachedPlaytimeTopService(this, playtimeService);
        this.topService.start();

        new PlaytimePlaceholder(playtimeService).register();

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        if (topService != null) {
            topService.shutdown();
        }
    }

    private void registerCommands() {
        PluginCommand playtime = getCommand("playtime");
        if (playtime != null) {
            playtime.setExecutor(new PlaytimeCommand(playtimeService));
        }

        PluginCommand playtimeTop = getCommand("playtimetop");
        if (playtimeTop != null) {
            playtimeTop.setExecutor(new PlaytimeTopCommand(
                    topService,
                    playtimeService,
                    () -> getConfig().getLong("top_cache.command_cooldown_seconds", 30L)
            ));
        }

        PluginCommand reload = getCommand("akatreload");
        if (reload != null) {
            reload.setExecutor(new ReloadConfigCommand(this, topService));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new NightLockListener(this, playtimeService), this);
        getServer().getPluginManager().registerEvents(new RestrictionListener(playtimeService), this);
    }
}
