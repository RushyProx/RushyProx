package net.rushnation.rushyprox.command;

import lombok.Getter;
import net.rushnation.rushyprox.command.annotation.Command;
import net.rushnation.rushyprox.command.exception.CommandException;

@Getter
public abstract class ProxyCommand {

    //Requested
    private final String name;
    private final String description;

    //Not Requested
    private String permission;
    private String[] alias;
    private String usage;

    private final Class<? extends ProxyCommand> clazz = this.getClass();

    public ProxyCommand() {
        if(!this.clazz.isAnnotationPresent(Command.class)) {
            throw new CommandException("Command class " + this.getClazz().getName() + " has no @Command Annotation");
        }

        this.name = this.clazz.getAnnotation(Command.class).name();
        this.description = this.clazz.getAnnotation(Command.class).description();

        if(this.getName().isEmpty() || this.getDescription().isEmpty()) {
            throw new CommandException("Config name or description cannot be Empty in class " + this.getClazz().getName());
        }

        if(!this.clazz.getAnnotation(Command.class).usage().isEmpty()) {
            this.usage = this.clazz.getAnnotation(Command.class).usage();
        }

        if(!this.clazz.getAnnotation(Command.class).permission().isEmpty()) {
            this.permission = this.clazz.getAnnotation(Command.class).permission();
        }

        if(this.clazz.getAnnotation(Command.class).alias().length != 0) {
            this.alias = this.clazz.getAnnotation(Command.class).alias();
        }
    }

    public abstract boolean run(CommandSender commandSender, String label, String[] args);
}
