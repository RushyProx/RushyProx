package net.rushnation.rushyprox.scheduler;

public interface ITask {

    void run();

    long getId();

    void cancel();

    void setHandler(ITaskHandler handler);

    ITaskHandler getHandler();

}
