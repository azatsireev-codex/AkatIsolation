package net.akat.service;

import net.akat.model.TopEntry;
import net.akat.service.scheduler.CancellableTask;
import net.akat.service.scheduler.ServerSchedulerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicReference;

public class CachedPlaytimeTopService implements PlaytimeTopService {

    private final Plugin plugin;
    private final PlaytimeService playtimeService;
    private final ServerSchedulerAdapter scheduler;

    private volatile int maxEntries;
    private volatile int batchSize;
    private volatile long refreshIntervalTicks;

    private final AtomicReference<List<TopEntry>> topCache = new AtomicReference<>(List.of());

    private CancellableTask refreshTicker;
    private CancellableTask batchTask;

    public CachedPlaytimeTopService(Plugin plugin, PlaytimeService playtimeService) {
        this.plugin = plugin;
        this.playtimeService = playtimeService;
        this.scheduler = new ServerSchedulerAdapter(plugin);
        loadConfigValues();
    }

    public void start() {
        triggerRefresh();
        restartRefreshTicker();
    }

    @Override
    public synchronized void triggerRefresh() {
        if (batchTask != null && !batchTask.isCancelled()) {
            return;
        }

        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        if (players.length == 0) {
            topCache.set(List.of());
            return;
        }

        PriorityQueue<TopEntry> heap = new PriorityQueue<>(Comparator.comparingLong(TopEntry::playtimeSeconds));
        int currentMaxEntries = maxEntries;
        int currentBatchSize = batchSize;

        batchTask = new BukkitTopBatchTask(scheduler, players, currentBatchSize, (player) -> {
            String name = player.getName() != null ? player.getName() : player.getUniqueId().toString();
            long seconds = playtimeService.getPlaytimeSeconds(player);
            TopEntry candidate = new TopEntry(name, seconds);

            if (heap.size() < currentMaxEntries) {
                heap.offer(candidate);
                return;
            }
            TopEntry smallest = heap.peek();
            if (smallest != null && candidate.playtimeSeconds() > smallest.playtimeSeconds()) {
                heap.poll();
                heap.offer(candidate);
            }
        }, () -> {
            List<TopEntry> result = new ArrayList<>(heap);
            result.sort((a, b) -> Long.compare(b.playtimeSeconds(), a.playtimeSeconds()));
            topCache.set(List.copyOf(result));
        }).start();
    }

    @Override
    public synchronized void reloadFromConfig() {
        loadConfigValues();
        restartRefreshTicker();
    }

    @Override
    public List<TopEntry> getTopEntries() {
        return topCache.get();
    }

    @Override
    public synchronized void shutdown() {
        if (refreshTicker != null) {
            refreshTicker.cancel();
        }
        if (batchTask != null) {
            batchTask.cancel();
        }
    }

    private void loadConfigValues() {
        this.maxEntries = Math.max(1, plugin.getConfig().getInt("top_cache.max_entries", 10));
        this.batchSize = Math.max(20, plugin.getConfig().getInt("top_cache.processing_batch_size", 200));
        long intervalMinutes = Math.max(1L, plugin.getConfig().getLong("top_cache.refresh_interval_minutes", 10L));
        this.refreshIntervalTicks = intervalMinutes * 60L * 20L;
    }

    private void restartRefreshTicker() {
        if (refreshTicker != null) {
            refreshTicker.cancel();
        }
        refreshTicker = scheduler.runAtFixedRate(this::triggerRefresh, refreshIntervalTicks, refreshIntervalTicks);
    }
}
