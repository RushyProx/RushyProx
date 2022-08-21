package net.rushnation.rushyprox.console;

import lombok.SneakyThrows;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.player.ProxyPlayer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public class Console extends SimpleTerminalConsole {

    private final ProxyServer PROXY;
    private final ConsoleThread consoleThread;
    public static ProxyPlayer proxyPlayer;

    @SneakyThrows
    public Console(ProxyServer PROXY) {
        this.PROXY = PROXY;

        ProxyServer.getProxyServer().getLogger().info("Starting console thread now.");
        this.consoleThread = new ConsoleThread(this);
        this.consoleThread.start();
        ProxyServer.getProxyServer().getLogger().info("Console thread successfully started.");

    }

    @Override
    protected boolean isRunning() {
        return this.consoleThread.isAlive();
    }

    @SneakyThrows
    @Override
    protected void runCommand(String command) {
        this.PROXY.getCommandManager().dispatchCommand(this.PROXY.getConsoleCommandSender(), command);
    }

    @SneakyThrows
    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        builder.appName("RushyProx");
        builder.option(LineReader.Option.HISTORY_BEEP, false);
        builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true);
        builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true);

        return super.buildReader(builder);
    }

    @Override
    protected void shutdown() {
        PROXY.shutdown();
    }
}
