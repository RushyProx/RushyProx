package net.rushnation.rushyprox.command;

public interface CommandSender {

    void sendMessage(String message);

    boolean hasPermission(String permission);
}
