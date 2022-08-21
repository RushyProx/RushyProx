package net.rushnation.rushyprox.event;

@FunctionalInterface
public interface EventAction<T extends IEvent> {

    void accept(T event);

}
