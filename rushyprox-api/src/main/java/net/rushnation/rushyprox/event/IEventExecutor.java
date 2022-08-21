package net.rushnation.rushyprox.event;

import net.rushnation.rushyprox.event.EventAction;
import net.rushnation.rushyprox.event.EventPriority;
import net.rushnation.rushyprox.event.IEvent;

import java.util.concurrent.CompletableFuture;

public interface IEventExecutor {

    <T extends IEvent> CompletableFuture<T> handleEvent(T event);

    <T extends IEvent> void handleEventWithPriority(T event, EventPriority priority);

    <T extends IEvent> CompletableFuture<T> handleSynchronously(T event);

    void register(EventAction<IEvent> action, EventPriority eventPriority);

}
