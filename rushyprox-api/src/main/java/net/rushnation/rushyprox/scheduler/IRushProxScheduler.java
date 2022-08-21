package net.rushnation.rushyprox.scheduler;

import net.rushnation.rushyprox.plugin.Plugin;

public interface IRushProxScheduler {

    ITaskHandler scheduleSynchronousTask(Runnable task, Plugin plugin);

    ITaskHandler scheduleAsynchronousTask(Runnable task, Plugin plugin);

    ITaskHandler scheduleDelayedTask(Runnable runnable, int delay, boolean async, Plugin plugin);

    ITaskHandler scheduleRepeatingTask(Runnable task, int period, Plugin plugin);

    ITaskHandler scheduleRepeatingAsynchronousTask(Runnable task, int period, Plugin plugin);

    ITaskHandler scheduleRepeatingDelayedTask(Runnable task, int delay, int period, boolean async, Plugin plugin);

}
