package net.rushnation.rushyprox.event;

public interface IEventManager {

    <T extends IEvent> void call(T event);

    <T extends IEvent> void on(Class<T> event, EventAction<T> eventAction);

    <T extends IEvent> void on(Class<T> event, EventPriority priority, EventAction<T> eventAction);

}
