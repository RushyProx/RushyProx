package net.rushnation.rushyprox.command;

import lombok.Getter;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.command.basic.ProxyEndCommand;
import net.rushnation.rushyprox.command.exception.CommandException;

import java.util.HashMap;

public class CommandManager {

    @Getter
    private final HashMap<String, ProxyCommand> commands = new HashMap<>();

    @Getter
    private final HashMap<String, ProxyCommand> aliasCommands = new HashMap<>();

    private final ProxyServer proxy;

    public CommandManager(ProxyServer proxy) {
        this.proxy = proxy;
        this.registerDefaultCommands();
        ProxyServer.getProxyServer().getLogger().info("Registered all Default Commands");
    }

    public void registerCommand(String name, ProxyCommand command) {
        if(this.commands.containsKey(name)) {
            return;
        }

        for (String alias : command.getAlias()) {
            this.registerAlias(command, alias);
        }

        this.commands.put(name, command);
    }

    private void registerDefaultCommands() {
        this.registerCommands(
                new ProxyEndCommand(this.proxy)
        );
    }

    private void registerCommands(ProxyCommand... proxyCommands) {
        for(ProxyCommand command: proxyCommands) {
            this.registerCommand(command.getName(), command);
        }
    }

    public void registerAlias(ProxyCommand command, String alias) {
        if(this.getAliasCommands().containsKey(alias)) return;

        this.getAliasCommands().put(alias, command);
    }

    public void dispatchCommand(CommandSender commandSender, String cmd) {
        if(cmd.isEmpty()) {
            return;
        }

        String[] args = cmd.split(" ");
        if (args.length < 1) {
            return;
        }

        ProxyCommand command = this.getCommands().get(cmd);
        ProxyCommand aliasCommand = this.getAliasCommands().get(cmd);

        if(command != null) {
            this.runCommand(commandSender, command, command.getName(), args);
            return;
        }

        if(aliasCommand != null) {
            this.runCommand(commandSender, aliasCommand, aliasCommand.getName(), args);
            return;
        }

        commandSender.sendMessage("Command was not found");
    }

    private void runCommand(CommandSender sender, ProxyCommand command, String label, String[] args) {
        if(command.getPermission() != null && !command.getPermission().isEmpty()) {
            if(!sender.hasPermission(command.getPermission())) {
                sender.sendMessage("You have no Permission for this Command contact a Admin");
                return;
            }
        }

       try {
           boolean run = command.run(sender, label, args);
           if(!run && !command.getUsage().isEmpty()) {
               sender.sendMessage(command.getUsage());
           }
       }catch (CommandException e) {
           e.printStackTrace();
       }
    }
}
