package net.akat.service.scheduler;

public interface CancellableTask {
    void cancel();

    boolean isCancelled();
}
