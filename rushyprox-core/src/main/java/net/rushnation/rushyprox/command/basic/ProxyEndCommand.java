package net.rushnation.rushyprox.command.basic;

import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.command.CommandSender;
import net.rushnation.rushyprox.command.ConsoleCommandSender;
import net.rushnation.rushyprox.command.ProxyCommand;
import net.rushnation.rushyprox.command.annotation.Command;
import net.rushnation.rushyprox.player.ProxyPlayer;

@RequiredArgsConstructor
@Command(name = "end", description = "Shutdown the proxy")
public class ProxyEndCommand extends ProxyCommand {

    private final ProxyServer server;

    @Override
    public boolean run(CommandSender commandSender, String alias, String[] args) {
        if (commandSender instanceof ConsoleCommandSender) {
            ProxyServer.getProxyServer().getLogger().info("Shutting down proxy server.");
            ProxyServer.getProxyServer().shutdown();
        }

        return true;
    }
}
