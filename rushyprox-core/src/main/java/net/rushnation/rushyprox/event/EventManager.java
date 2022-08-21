package net.rushnation.rushyprox.event;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager implements IEventManager {

    private final Map<Class<? extends IEvent>, EventExecutor> eventExecutors = Collections.synchronizedMap(new ConcurrentHashMap<>());

    @Override
    public <T extends IEvent> void call(T event) {
        IEventExecutor eventExecutor = this.eventExecutors.computeIfAbsent(event.getClass(), EventExecutor::new);
        eventExecutor.handleEvent(event);
    }

    @Override
    public <T extends IEvent> void on(Class<T> event, EventAction<T> eventAction) {
        this.on(event, EventPriority.NORMAL, eventAction);
    }

    @Override
    public <T extends IEvent> void on(Class<T> event, EventPriority priority, EventAction<T> eventAction) {
        EventExecutor eventExecutor = this.eventExecutors.computeIfAbsent(event, EventExecutor::new);
        eventExecutor.register((EventAction<IEvent>) eventAction, priority);
    }
}
