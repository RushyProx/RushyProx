package net.rushnation.rushyprox.command.exception;

public class CommandException extends RuntimeException {

    public CommandException(String reason) {
        super(reason);
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
