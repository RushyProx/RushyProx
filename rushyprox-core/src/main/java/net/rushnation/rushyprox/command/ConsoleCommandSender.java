package net.rushnation.rushyprox.command;

import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;

@RequiredArgsConstructor
public class ConsoleCommandSender implements net.rushnation.rushyprox.command.type.ConsoleCommandSender {

    private final ProxyServer proxy;

    @Override
    public void sendMessage(String message) {
        this.proxy.getLogger().info(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

}
