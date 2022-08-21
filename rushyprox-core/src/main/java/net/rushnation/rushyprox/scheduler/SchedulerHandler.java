package net.rushnation.rushyprox.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rushnation.rushyprox.ProxyServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SchedulerHandler {

    private final ProxyServer proxyServer;
    private RushProxScheduler rushProxScheduler;

    private ScheduledExecutorService tickExecutorService;
    @Getter
    private int currentTick;
    private ScheduledFuture<?> tickScheduledFuture;
    @Setter
    private boolean shutdown = false;

    public void prepareTickExecutor() {
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setNameFormat("");
        this.tickExecutorService = Executors.newScheduledThreadPool(1, threadFactoryBuilder.build());
    }

    public void prepareTickFuture() {
        this.tickScheduledFuture = this.tickExecutorService.scheduleAtFixedRate(this::processTick, 50, 50, TimeUnit.MILLISECONDS);
    }

    private void processTick() {
        if (this.shutdown && !this.tickScheduledFuture.isCancelled()) {
            this.tickScheduledFuture.cancel(false);
            this.proxyServer.shutdown();
        }

        try {
            this.tick(++this.currentTick);
        } catch (Exception e) {
            this.proxyServer.getLogger().warn("There was a error while ticking proxy", e);
        }
    }

    private void tick(int currentTick) {
        this.rushProxScheduler.tick(currentTick);
    }

    public void setRushProxScheduler(RushProxScheduler rushProxScheduler) {
        this.rushProxScheduler = rushProxScheduler;
    }
}
