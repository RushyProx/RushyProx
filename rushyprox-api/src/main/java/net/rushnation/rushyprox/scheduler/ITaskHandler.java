package net.rushnation.rushyprox.scheduler;

import net.rushnation.rushyprox.plugin.Plugin;

public interface ITaskHandler {

    void run(int tick);

    void cancelTask();

    long getTaskId();

    Runnable getTask();

    boolean isAsynchronous();

    int getLastTick();

    int getNextTick();

    int getPeriod();

    int getDelay();

    Plugin getPlugin();

    boolean isCanceled();

    boolean hasDelay();

    boolean isRepeating();

    void setLastTick(int tick);

    void setNextTick(int tick);

    void setPeriod(int period);

    void setDelay(int delay);

    boolean calculateNextTaskTick(int current);

}
