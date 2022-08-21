package net.rushnation.rushyprox.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Command {

    String name();

    String description();

    String usage() default "";

    String permission() default "";

    String[] alias() default "";

}
