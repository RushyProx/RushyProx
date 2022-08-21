package net.rushnation.rushyprox;

import net.rushnation.rushyprox.event.EventManager;
import net.rushnation.rushyprox.event.EventPriority;

public class Bootstrap {

    public static final EventManager eventManager = new EventManager();

    public Bootstrap() {
        ProxyServer proxyServer = new ProxyServer();
        proxyServer.bootProxy();
    }

    public static void main(String[] args) {
        // Start Proxy server here
        new Bootstrap();
    }

}
