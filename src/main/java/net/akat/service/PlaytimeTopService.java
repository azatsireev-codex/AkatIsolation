package net.akat.service;

import net.akat.model.TopEntry;

import java.util.List;

public interface PlaytimeTopService {
    List<TopEntry> getTopEntries();

    void triggerRefresh();

    void reloadFromConfig();

    void shutdown();
}
