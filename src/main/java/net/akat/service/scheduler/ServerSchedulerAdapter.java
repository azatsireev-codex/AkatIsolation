package net.akat.service.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class ServerSchedulerAdapter {

    private final Plugin plugin;
    private final Object globalRegionScheduler;
    private final Method foliaRunAtFixedRate;

    public ServerSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;

        Object detectedScheduler = null;
        Method detectedMethod = null;

        try {
            Method getGlobalRegionScheduler = Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler");
            detectedScheduler = getGlobalRegionScheduler.invoke(Bukkit.getServer());
            detectedMethod = detectedScheduler.getClass().getMethod(
                    "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class
            );
            plugin.getLogger().info("Detected Folia global scheduler API. Using Folia-compatible scheduling.");
        } catch (ReflectiveOperationException ignored) {
            plugin.getLogger().info("Folia scheduler API not detected. Falling back to Bukkit scheduler.");
        }

        this.globalRegionScheduler = detectedScheduler;
        this.foliaRunAtFixedRate = detectedMethod;
    }

    public CancellableTask runAtFixedRate(Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (globalRegionScheduler != null && foliaRunAtFixedRate != null) {
            return runOnFoliaGlobalScheduler(runnable, initialDelayTicks, periodTicks);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks);
        return new CancellableTask() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }

    private CancellableTask runOnFoliaGlobalScheduler(Runnable runnable, long initialDelayTicks, long periodTicks) {
        try {
            Consumer<Object> consumer = scheduledTask -> runnable.run();
            Object foliaTask = foliaRunAtFixedRate.invoke(globalRegionScheduler, plugin, consumer, initialDelayTicks, periodTicks);
            Method cancelMethod = foliaTask.getClass().getMethod("cancel");
            Method cancelledMethod;
            try {
                cancelledMethod = foliaTask.getClass().getMethod("isCancelled");
            } catch (NoSuchMethodException noIsCancelled) {
                cancelledMethod = foliaTask.getClass().getMethod("cancelled");
            }

            Method finalCancelledMethod = cancelledMethod;
            return new CancellableTask() {
                @Override
                public void cancel() {
                    try {
                        cancelMethod.invoke(foliaTask);
                    } catch (ReflectiveOperationException ignored) {
                    }
                }

                @Override
                public boolean isCancelled() {
                    try {
                        Object result = finalCancelledMethod.invoke(foliaTask);
                        return result instanceof Boolean && (Boolean) result;
                    } catch (ReflectiveOperationException ignored) {
                        return false;
                    }
                }
            };
        } catch (ReflectiveOperationException e) {
            BukkitTask fallback = Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks);
            return new CancellableTask() {
                @Override
                public void cancel() {
                    fallback.cancel();
                }

                @Override
                public boolean isCancelled() {
                    return fallback.isCancelled();
                }
            };
        }
    }
}
