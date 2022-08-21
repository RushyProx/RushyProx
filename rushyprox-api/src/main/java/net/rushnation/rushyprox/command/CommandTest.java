package net.rushnation.rushyprox.command;

import net.rushnation.rushyprox.player.IProxyPlayer;

public class CommandTest extends ProxyCommand{

    @Override
    public boolean run(CommandSender commandSender, String alias, String[] args) {
        IProxyPlayer player = (IProxyPlayer) commandSender;
        return true;
    }
}
