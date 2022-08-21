package net.rushnation.rushyprox.event;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class EventExecutor implements IEventExecutor {

    private final Class<? extends IEvent> event;

    private final Map<EventPriority, List<EventAction<IEvent>>> priorityEventHandlers = new EnumMap<>(EventPriority.class);

    @Override
    public <T extends IEvent> CompletableFuture<T> handleEvent(T event) {
        Preconditions.checkArgument(this.event.isInstance(event), "Cannot handle event with wrong instance");

        if (!event.isHandledAsync()) {
            return this.handleSynchronously(event);
        }

        CompletableFuture<T> eventFuture = new CompletableFuture<>();

        CompletableFuture.supplyAsync(() -> {
            for (EventPriority priority : EventPriority.values()) {
                this.handleEventWithPriority(event, priority);
            }

            return event;
        }).thenAccept(future -> future.complete(eventFuture)).whenComplete((result, error) -> {
            if (error != null && !eventFuture.isDone()) {
                eventFuture.completeExceptionally(error);
            }
        });

        return eventFuture;
    }

    @Override
    public <T extends IEvent> void handleEventWithPriority(T event, EventPriority priority) {
        List<EventAction<IEvent>> events = this.priorityEventHandlers.get(priority);
        if (events == null) {
            return;
        }

        events.forEach(eventAction -> eventAction.accept(event));
    }

    @Override
    public <T extends IEvent> CompletableFuture<T> handleSynchronously(T event) {
        if (!event.isFuture()) {
            for (EventPriority priority : EventPriority.values()) {
                this.handleEventWithPriority(event, priority);
            }

            return null;
        }

        try {
            for (EventPriority priority : EventPriority.values()) {
                this.handleEventWithPriority(event, priority);
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }

        if (event.getFutures().isEmpty()) {
            return CompletableFuture.completedFuture(event);
        }

        CompletableFuture<T> eventFuture = new CompletableFuture<>();
        event.complete(eventFuture);
        return eventFuture;
    }

    @Override
    public void register(EventAction<IEvent> action, EventPriority eventPriority) {
        List<EventAction<IEvent>> handlerList = this.priorityEventHandlers.computeIfAbsent(eventPriority, priority -> new ArrayList<>());
        if (!handlerList.contains(action)) {
            handlerList.add(action);
        }
    }
}
