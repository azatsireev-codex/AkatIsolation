package net.akat.service;

import net.akat.service.scheduler.CancellableTask;
import net.akat.service.scheduler.ServerSchedulerAdapter;
import org.bukkit.OfflinePlayer;

import java.util.function.Consumer;

public class BukkitTopBatchTask {

    private final ServerSchedulerAdapter scheduler;
    private final OfflinePlayer[] players;
    private final int batchSize;
    private final Consumer<OfflinePlayer> processor;
    private final Runnable onFinish;

    private int cursor = 0;

    public BukkitTopBatchTask(ServerSchedulerAdapter scheduler,
                              OfflinePlayer[] players,
                              int batchSize,
                              Consumer<OfflinePlayer> processor,
                              Runnable onFinish) {
        this.scheduler = scheduler;
        this.players = players;
        this.batchSize = batchSize;
        this.processor = processor;
        this.onFinish = onFinish;
    }

    public CancellableTask start() {
        final CancellableTask[] selfRef = new CancellableTask[1];
        selfRef[0] = scheduler.runAtFixedRate(() -> {
            int processed = 0;
            while (cursor < players.length && processed < batchSize) {
                processor.accept(players[cursor]);
                cursor++;
                processed++;
            }

            if (cursor >= players.length) {
                onFinish.run();
                selfRef[0].cancel();
            }
        }, 1L, 1L);
        return selfRef[0];
    }
}
