package net.akat.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class BukkitTopBatchTask {

    private final Plugin plugin;
    private final OfflinePlayer[] players;
    private final int batchSize;
    private final Consumer<OfflinePlayer> processor;
    private final Runnable onFinish;

    private int cursor = 0;

    public BukkitTopBatchTask(Plugin plugin,
                              OfflinePlayer[] players,
                              int batchSize,
                              Consumer<OfflinePlayer> processor,
                              Runnable onFinish) {
        this.plugin = plugin;
        this.players = players;
        this.batchSize = batchSize;
        this.processor = processor;
        this.onFinish = onFinish;
    }

    public BukkitTask start() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                int processed = 0;
                while (cursor < players.length && processed < batchSize) {
                    processor.accept(players[cursor]);
                    cursor++;
                    processed++;
                }

                if (cursor >= players.length) {
                    onFinish.run();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
