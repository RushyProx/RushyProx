package net.rushnation.rushyprox.plugin;

import lombok.Data;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Data
public class PluginData {

    private final File file;

    private String name = "N/A";
    private String description = "N/A";
    private String author = "N/A";
    private String version = "N/A";
    private String[] authors = new String[]{};
    private String[] dependOn = new String[]{};
    private PluginStartupPriority.StartupPriority startupPriority;

    // Module stuff
    private String moduleName;
    private boolean hasModuleInfo;
    private Set<String> packages = new HashSet<>();
    private Set<File> moduleDependencies;
    private Class<?> mainClass;

}
