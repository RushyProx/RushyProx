package net.rushnation.rushyprox.scheduler;

import com.google.common.base.Preconditions;

public abstract class Task implements ITask, Runnable {

    private ITaskHandler taskHandler;

    public abstract void whenRun();

    public void whenChancel() {

    }

    @Override
    public void run() {
        this.whenRun();
    }

    @Override
    public void setHandler(ITaskHandler handler) {
        Preconditions.checkArgument(this.taskHandler == null, "Cannot set TaskHandler twice");

        this.taskHandler = handler;
    }

    @Override
    public ITaskHandler getHandler() {
        return this.taskHandler;
    }

    @Override
    public long getId() {
        return this.taskHandler != null ? this.taskHandler.getTaskId() : -1;
    }

    @Override
    public void cancel() {
        this.taskHandler.cancelTask();
    }
}
