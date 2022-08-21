package net.rushnation.rushyprox.event;

import com.google.common.base.Preconditions;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class Event implements IEvent {

    private final List<CompletableFuture<Void>> futures;
    private boolean isCancelled;

    public Event() {
        this.futures = this.isFuture() ? Collections.synchronizedList(new ArrayList<>()) : null;
    }

    @Override
    public void setCancelled() {
        this.setCancelled(true);
    }

    @Override
    public void setCancelled(boolean cancel) {
        Preconditions.checkArgument((this instanceof Cancelable), "Event is not cancelable!");
        this.isCancelled = cancel;
    }

    @Override
    public void addFuture(CompletableFuture<Void> future) {
        Preconditions.checkArgument(this.isFuture(), "Cannot add future to this event cuz it's not @HandleAsync or @FutureEvent");
        this.futures.add(future);
    }

    @Override
    public <T extends IEvent> void complete(CompletableFuture<T> future) {
        Preconditions.checkArgument(this.isFuture(), "Cannot complete future of this event cuz it's not @HandleAsync or @FutureEvent");

        if (this.futures.isEmpty()) {
            future.complete((T) this);
            return;
        }

        CompletableFuture.allOf(this.futures.toArray(new CompletableFuture[0])).whenComplete((result, error) -> {
            if (error == null) {
                future.complete((T) this);
                return;
            }

            future.completeExceptionally(error);
        });
    }

    @Override
    public List<CompletableFuture<Void>> getFutures() {
        Preconditions.checkArgument(this.isFuture(), "Cannot get future list of this event cuz it's not @HandleAsync or @FutureEvent");
        return this.futures;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isHandledAsync() {
        return this.getClass().isAnnotationPresent(HandleAsync.class);
    }

    @Override
    public boolean isFuture() {
        return this.getClass().isAnnotationPresent(FutureEvent.class);
    }
}
