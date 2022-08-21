package net.rushnation.rushyprox.plugin;

import net.rushnation.rushyprox.ProxyServer;
import net.rushnation.rushyprox.config.ConfigFile;
import net.rushnation.rushyprox.config.YamlConfig;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ModuleVisitor;
import org.springframework.asm.Opcodes;

import java.io.*;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    private final ProxyServer PROXY;

    // Here classes will be cached, so they don't need to be loaded twice
    private final Map<String, Class<?>> cachedClasses = Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<String, PluginClassLoader> cachedLoaders = Collections.synchronizedMap(new ConcurrentHashMap<>());

    public PluginLoader(ProxyServer PROXY) {
        this.PROXY = PROXY;
    }

    public boolean isJarFile(File file) {
        return file.getName().endsWith(".jar");
    }

    public Plugin loadPlugin(File file) {

        PluginData pluginData = this.getPluginData(file);

        if (pluginData == null) {
            ProxyServer.getProxyServer().getLogger().error("Could not load plugin {}", file.getName());
            return null;
        }

        try {
            Class<? extends Plugin> casted = pluginData.getMainClass().asSubclass(Plugin.class);
            Plugin plugin = casted.getDeclaredConstructor().newInstance();
            plugin.init(pluginData, this.PROXY, file);

            return plugin;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            ProxyServer.getProxyServer().getLogger().error("Could not construct plugin instance of {} -> {}", pluginData.getMainClass().getName(), e.getMessage());
            ProxyServer.getProxyServer().getLogger().error("{}", e, e);
        }

        return null;
    }


    /**
     * @param file the jar file that should be loaded
     * @return the plugin data of provided jar file
     * @Credits to geNAZt and GoMint Team
     */
    private PluginData getPluginData(File file) {
        // Open the jar
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> jarEntries = jar.entries();

            // It seems like the jar is empty
            if (!jarEntries.hasMoreElements()) {
                ProxyServer.getProxyServer().getLogger().warn("Could not load Plugin. File {} is empty", file);
                return null;
            }

            PluginData data = new PluginData(file);
            File depModules = new File("modules/plugin/" + file.getName().replace(".jar", ""));

            ModuleDetector detector = new ModuleDetector();
            ModuleDescriptor descriptor = detector.deriveModuleDescriptor(jar);
            data.setModuleName(descriptor.name());

            PluginClassLoader pluginClassLoader = new PluginClassLoader(this.getClass().getClassLoader(), file);

            // Try to read every file in the jar
            try {
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();

                    // If we found a jar inside a jar extract it
                    if (jarEntry != null && jarEntry.getName().endsWith(".jar")) {
                        depModules.mkdirs(); // We simply ignore it, if the creation failed the extraction will error either way

                        File jarFile = Paths.get(depModules.getAbsolutePath(), jarEntry.getName()).toFile();
                        jarFile.getParentFile().mkdirs(); // Ignore, it will fail in the copy

                        try (FileOutputStream toFile = new FileOutputStream(jarFile)) {
                            jar.getInputStream(jarEntry).transferTo(toFile);
                        }

                        if (data.getModuleDependencies() == null) {
                            data.setModuleDependencies(new HashSet<>());
                        }

                        data.getModuleDependencies().add(jarFile);
                    }

                    // When the entry is valid and ends with a .class its a java class and we need to scan it
                    if (jarEntry != null && jarEntry.getName().endsWith(".class")) {
                        ClassReader cr = new ClassReader(new DataInputStream(jar.getInputStream(jarEntry)));

                        // Sort out *info.class files
                        if (cr.getSuperName() == null) {
                            cr.accept(new ClassVisitor(Opcodes.ASM7) {
                                @Override
                                public ModuleVisitor visitModule(String name, int access, String version) {
                                    data.setModuleName(name);
                                    data.setHasModuleInfo(true);
                                    return super.visitModule(name, access, version);
                                }
                            }, 0);

                            continue;
                        } else {
                            cr.accept(new ClassVisitor(Opcodes.ASM7) {
                                @Override
                                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                    String packageName = name.replace("/", ".");
                                    int lastIndex = packageName.lastIndexOf(".");
                                    packageName = packageName.substring(0, lastIndex);

                                    data.getPackages().add(packageName);
                                    super.visit(version, access, name, signature, superName, interfaces);
                                }
                            }, 0);
                        }

                        // Does this class extend the plugin class?
                        if (cr.getSuperName().equals(Plugin.class.getName().replaceAll("\\.", "/"))) {
                            Class<?> pluginMainClass = pluginClassLoader.loadClass(cr.getClassName().replaceAll("/", "."));

                            if (!pluginMainClass.isAnnotationPresent(PluginMain.class)) {
                                ProxyServer.getProxyServer().getLogger().warn("Class {} in plugin {} does not annotate @PluginMain", pluginMainClass.getName(), file.getName());
                                return null;
                            }

                            PluginMain pluginBase = pluginMainClass.getAnnotation(PluginMain.class);
                            String pluginName = pluginBase.value();

                            data.setName(pluginName);
                            data.setMainClass(pluginMainClass);
                            data.setStartupPriority(PluginStartupPriority.StartupPriority.NORMAL);

                            if (pluginMainClass.isAnnotationPresent(PluginAuthor.class)) {
                                PluginAuthor pluginAuthor = pluginMainClass.getAnnotation(PluginAuthor.class);

                                data.setAuthor(pluginAuthor.value());
                                data.setAuthors(pluginAuthor.authors());
                            }

                            if (pluginMainClass.isAnnotationPresent(PluginDescription.class)) {
                                PluginDescription pluginDescription = pluginMainClass.getAnnotation(PluginDescription.class);

                                data.setDescription(pluginDescription.value());
                            }

                            if (pluginMainClass.isAnnotationPresent(PluginVersion.class)) {
                                PluginVersion pluginVersion = pluginMainClass.getAnnotation(PluginVersion.class);

                                data.setVersion(pluginVersion.value());
                            }

                            if (pluginMainClass.isAnnotationPresent(PluginDepend.class)) {
                                PluginDepend pluginDepend = pluginMainClass.getAnnotation(PluginDepend.class);

                                data.setDependOn(pluginDepend.value());
                            }

                            if (pluginMainClass.isAnnotationPresent(PluginStartupPriority.class)) {
                                PluginStartupPriority pluginStartupPriority = pluginMainClass.getAnnotation(PluginStartupPriority.class);

                                data.setStartupPriority(pluginStartupPriority.value());
                            }
                        } else {
                            // Search here for commands later on

                            if (cr.getSuperName().equals(YamlConfig.class.getName().replaceAll("\\.", "/"))) {
                                Class<?> configClass = pluginClassLoader.loadClass(cr.getClassName().replaceAll("/", "."));

                                if (configClass.isAnnotationPresent(ConfigFile.class)) {
                                    configClass.getSuperclass().getDeclaredMethod("load").invoke(configClass.newInstance());
                                    ProxyServer.getProxyServer().getLogger().info("Successfully loaded config {}.", configClass.getName());
                                } else {
                                    ProxyServer.getProxyServer().getLogger().warn("Found config {} but it does not annotate @ConfigFile", configClass.getName());
                                }

                            }

                        }
                    }
                }

                // Check if we found a valid plugin
                if (data.getMainClass() != null) {
                    return data;
                }

                return null;
            } catch (IOException e) {
                ProxyServer.getProxyServer().getLogger().warn("Could not load Plugin. File " + file + " is corrupted", e);
                return null;
            }
        } catch (Exception e) {
            ProxyServer.getProxyServer().getLogger().warn("Could not load plugin from file " + file, e);
            return null;
        }
    }
}
