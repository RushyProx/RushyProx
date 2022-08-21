package net.rushnation.rushyprox.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginStartupPriority {

    StartupPriority value() default StartupPriority.NORMAL;

    enum StartupPriority {
        HIGHEST,
        NORMAL,
        LOWEST
    }
}
