package net.rushnation.rushyprox.event.list;

import lombok.Getter;
import net.rushnation.rushyprox.event.Event;

import java.net.InetSocketAddress;

@Getter
public class ProxyPingEvent extends Event {

    private final InetSocketAddress address;

    public ProxyPingEvent(InetSocketAddress address) {
        this.address = address;
    }
}
