package net.rushnation.rushyprox.console;

public class ConsoleThread extends Thread {

    private final Console console;

    public ConsoleThread(Console console) {
        this.console = console;
    }

    @Override
    public void run() {
        this.console.start();
    }
}
