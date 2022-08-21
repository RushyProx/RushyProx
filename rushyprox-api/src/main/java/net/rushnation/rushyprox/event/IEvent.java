package net.rushnation.rushyprox.event;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IEvent {

    void setCancelled();

    void setCancelled(boolean cancel);

    void addFuture(CompletableFuture<Void> future);

    <T extends IEvent> void complete(CompletableFuture<T> future);

    List<CompletableFuture<Void>> getFutures();

    boolean isCancelled();

    boolean isHandledAsync();

    boolean isFuture();

}
