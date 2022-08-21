package net.rushnation.rushyprox.scheduler;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class RushProxScheduler implements IRushProxScheduler {

    private final ExecutorService asyncExecutor;

    private final SchedulerHandler schedulerHandler;
    private final AtomicLong currentTaskId = new AtomicLong(0);
    private final Map<Integer, LinkedList<TaskHandler>> assignedTasks = Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<Long, TaskHandler> taskHandlerMap = Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final LinkedList<TaskHandler> runningTasks = new LinkedList<>();

    public RushProxScheduler(SchedulerHandler schedulerHandler) {
        this.schedulerHandler = schedulerHandler;

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
        builder.setNameFormat("RushyProx-Async-Executor");
        int idleThreads = 16;
        this.asyncExecutor = new ThreadPoolExecutor(idleThreads, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), builder.build());
    }

    @Override
    public ITaskHandler scheduleSynchronousTask(Runnable task, Plugin plugin) {
        return this.createTask(task, 0, 0, false, plugin);
    }

    @Override
    public ITaskHandler scheduleAsynchronousTask(Runnable task, Plugin plugin) {
        return this.createTask(task, 0, 0, true, plugin);
    }

    @Override
    public ITaskHandler scheduleDelayedTask(Runnable task, int delay, boolean async, Plugin plugin) {
        return this.createTask(task, delay, 0, async, plugin);
    }

    @Override
    public ITaskHandler scheduleRepeatingTask(Runnable task, int period, Plugin plugin) {
        return this.createTask(task, 0, period, false, plugin);
    }

    @Override
    public ITaskHandler scheduleRepeatingAsynchronousTask(Runnable task, int period, Plugin plugin) {
        return this.createTask(task, 0, period, true, plugin);
    }

    @Override
    public ITaskHandler scheduleRepeatingDelayedTask(Runnable task, int delay, int period, boolean async, Plugin plugin) {
        return this.createTask(task, delay, period, async, plugin);
    }

    public ITaskHandler createTask(Runnable runnable, int delay, int period, boolean asynchronous, Plugin plugin) {

        if(delay < 0 || period < 0) {
            throw new RuntimeException("Cannot create task with negative period or delay.");
        }

        long taskId = this.currentTaskId.getAndIncrement();
        int current = this.schedulerHandler.getCurrentTick();

        TaskHandler taskHandler = new TaskHandler(plugin, runnable, asynchronous, taskId);
        taskHandler.setDelay(delay);
        taskHandler.setPeriod(period);
        taskHandler.setNextTick(!taskHandler.hasDelay() ? current : current + delay);

        this.runningTasks.add(taskHandler);
        this.taskHandlerMap.put(taskId, taskHandler);

        return taskHandler;
    }

    public void tick(int currentTick) {
        TaskHandler taskHandler = null;

        while ((taskHandler = this.runningTasks.poll()) != null) {
            int tick = Math.max(currentTick, taskHandler.getNextTick());

            this.assignedTasks.computeIfAbsent(tick, num -> new LinkedList<>()).add(taskHandler);
        }

        LinkedList<TaskHandler> queue = this.assignedTasks.remove(currentTick);

        if (queue != null) {
            queue.forEach(handler -> {
                this.runTask(currentTick, handler);
            });
        }
    }

    public int getCurrentTick() {
        return this.schedulerHandler.getCurrentTick();
    }

    public void runTask(int tick, TaskHandler taskHandler) {
        if (taskHandler.isCanceled()) {
            this.taskHandlerMap.remove(taskHandler.getTaskId());
            return;
        }

        if (!taskHandler.isAsynchronous()) {
            Thread.currentThread().setName(String.format("Task-%s", taskHandler.getTaskId()));
            taskHandler.run(tick);
        } else {
            this.asyncExecutor.execute(() -> taskHandler.run(tick));
        }

        if (taskHandler.calculateNextTaskTick(tick)) {
            this.runningTasks.add(taskHandler);
            return;
        }

        this.taskHandlerMap.remove(taskHandler.getTaskId()).cancelTask();
    }

    public void terminatePluginTasks(Plugin targetPlugin) {
        this.taskHandlerMap.values().forEach(taskHandler -> {
            // Check if equals does work if not use name
            if (taskHandler.getPlugin().equals(targetPlugin)) {
                taskHandler.cancelTask();
                this.taskHandlerMap.remove(taskHandler.getTaskId());
                this.runningTasks.remove(taskHandler);
            }
        });
    }

    public void close() {
        ProxyServer.getProxyServer().getLogger().warn("Closing scheduler service now.");

        this.asyncExecutor.shutdown();

        int times = 30;

        while (times-- > 0 && !this.asyncExecutor.isTerminated()) {
            try {
                this.asyncExecutor.awaitTermination(120, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
