package net.rushnation.rushyprox.scheduler;

import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.plugin.Plugin;

public class TaskHandler implements ITaskHandler {

    private final Runnable task;
    private final boolean isAsynchronous;
    private final long id;
    private final Plugin plugin;

    private int lastTick;
    private int nextTick;

    private int period;
    private int delay;

    private boolean canceled;

    public TaskHandler(Plugin plugin, Runnable task, boolean isAsynchronous, long id) {
        this.plugin = plugin;
        this.task = task;
        this.isAsynchronous = isAsynchronous;
        this.id = id;

        if (task instanceof Task) {
            ((Task) task).setHandler(this);
        }
    }

    @Override
    public void run(int tick) {
        this.lastTick = tick;

        try {
            task.run();
        } catch (Exception e) {
            ProxyServer.getProxyServer().getLogger().error("Error while running task with id {}", this.id, e);
        }
    }

    @Override
    public void cancelTask() {
        if (this.canceled) {
            ProxyServer.getProxyServer().getLogger().warn("Task with id {} is already canceled.", this.id);
            return;
        }

        if (this.task instanceof Task) {
            Task task = (Task) this.task;
            task.whenChancel();
        }

        this.canceled = true;
    }

    @Override
    public long getTaskId() {
        return this.id;
    }

    @Override
    public Runnable getTask() {
        return this.task;
    }

    @Override
    public boolean isAsynchronous() {
        return this.isAsynchronous;
    }

    @Override
    public int getLastTick() {
        return this.lastTick;
    }

    @Override
    public int getNextTick() {
        return this.nextTick;
    }

    @Override
    public int getPeriod() {
        return this.period;
    }

    @Override
    public int getDelay() {
        return this.delay;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isCanceled() {
        return this.canceled;
    }

    @Override
    public boolean hasDelay() {
        return this.delay > 0;
    }

    @Override
    public boolean isRepeating() {
        return this.period > 0;
    }

    @Override
    public void setLastTick(int tick) {
        this.lastTick = tick;
    }

    @Override
    public void setNextTick(int tick) {
        this.nextTick = tick;
    }

    @Override
    public void setPeriod(int period) {
        this.period = period;
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public boolean calculateNextTaskTick(int current) {

        if (!this.isRepeating() || this.isCanceled()) {
            return false;
        }

        this.nextTick = this.period + current;
        return true;
    }
}
