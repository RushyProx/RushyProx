package net.rushnation.rushyprox.plugin;

import lombok.RequiredArgsConstructor;
import net.rushnation.rushyprox.ProxyServer;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PluginManager implements IPluginManager {

    private final ProxyServer proxyServer;
    private final PluginLoader pluginLoader;
    private final String baseDirectory;

    private final Map<PluginStartupPriority.StartupPriority, List<Plugin>> priorityPluginMap = new EnumMap<>(PluginStartupPriority.StartupPriority.class);
    private final Map<Plugin, ClassLoader> pluginClassLoaders = new HashMap<>();
    private final Map<String, Plugin> loadedPlugins = Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<String, Plugin> enabledPlugins = Collections.synchronizedMap(new ConcurrentHashMap<>());

    // Add depend
    // Save plugins in list or something else

    public void loadAllPlugins(boolean enable) {
        File file = new File(this.baseDirectory);

        if (!file.exists()) {
            ProxyServer.getProxyServer().getLogger().info("Created plugin directory at {}. Success: {}", this.baseDirectory, file.mkdirs());
        }

        if (file.listFiles() == null) {
            ProxyServer.getProxyServer().getLogger().warn("Could not list files of plugin directory, result is null.");
            return;
        }

        for (File pluginFile : Objects.requireNonNull(file.listFiles())) {
            if (!this.pluginLoader.isJarFile(pluginFile) || !Files.isRegularFile(pluginFile.toPath())) {
                continue;
            }

            Plugin plugin = this.pluginLoader.loadPlugin(pluginFile);

            if (plugin == null) {
                continue;
            }

            if (this.isPluginLoadedOrEnabled(plugin.getPluginData().getName())) {
                ProxyServer.getProxyServer().getLogger().info("Could not enable {} (File: {}) cuz there's a duplicated plugin name -> (File: {})", plugin.getPluginData().getName(), file.getName(), this.loadedPlugins.get(plugin.getPluginData().getName()).getPluginFile().getName());
                continue;
            }

            try {
                ProxyServer.getProxyServer().getLogger().info("Loading {} Version: {}", plugin.getPluginData().getName(), plugin.getPluginData().getVersion());

                plugin.onLoad();
                this.pluginLoaded(plugin);

                if (this.priorityPluginMap.containsKey(plugin.getPluginData().getStartupPriority())) {
                    this.priorityPluginMap.get(plugin.getPluginData().getStartupPriority()).add(plugin);
                } else {
                    List<Plugin> pluginList = new ArrayList<>(Collections.emptyList());
                    pluginList.add(plugin);
                    this.priorityPluginMap.putIfAbsent(plugin.getPluginData().getStartupPriority(), pluginList);
                }
            } catch (Exception e) {
                ProxyServer.getProxyServer().getLogger().error("Error while loading {}", plugin.getPluginData().getName());
                ProxyServer.getProxyServer().getLogger().error("{}", e, e);

                this.forceDisable(plugin);
            }
        }

        if (enable) {
            this.enablePlugins();
        }
    }

    public void enablePlugins() {
        for (PluginStartupPriority.StartupPriority priority : PluginStartupPriority.StartupPriority.values()) {
            List<Plugin> plugins = this.priorityPluginMap.get(priority);

            if (plugins == null) {
                continue;
            }

            for (Plugin plugin : plugins) {
                try {
                    ProxyServer.getProxyServer().getLogger().info("Enabling {} Version: {}", plugin.getPluginData().getName(), plugin.getPluginData().getVersion());

                    plugin.onEnable();
                    this.pluginEnabled(plugin);
                } catch (Exception e) {
                    ProxyServer.getProxyServer().getLogger().error("Error while enabling {}", plugin.getPluginData().getName());
                    ProxyServer.getProxyServer().getLogger().error("{}", e, e);

                    this.forceDisable(plugin);
                }
            }
        }
    }

    public void disablePlugins(boolean force) {
        this.enabledPlugins.forEach((name, plugin) -> {

            if (!force) {
                this.disable(plugin);
            } else {
                this.forceDisable(plugin);
            }
        });
    }

    public void forceDisable(Plugin plugin) {
        ProxyServer.getProxyServer().getLogger().info("Disabling {}", plugin.getPluginData().getName());

        // Remove from every list ...
        this.loadedPlugins.remove(plugin.getPluginData().getName());
        this.enabledPlugins.remove(plugin.getPluginData().getName());

        // Stop tasks of plugin
        this.proxyServer.getRushProxScheduler().terminatePluginTasks(plugin);
    }

    public void disable(Plugin plugin) {
        ProxyServer.getProxyServer().getLogger().info("Disabling {}", plugin.getPluginData().getName());

        plugin.onDisable();
        this.loadedPlugins.remove(plugin.getPluginData().getName());
        this.enabledPlugins.remove(plugin.getPluginData().getName());

        // Stop tasks of plugin
        this.proxyServer.getRushProxScheduler().terminatePluginTasks(plugin);
    }

    public boolean isPluginLoadedOrEnabled(String name) {
        return this.loadedPlugins.containsKey(name) || this.enabledPlugins.containsKey(name);
    }

    public void pluginLoaded(Plugin plugin) {
        this.loadedPlugins.put(plugin.getPluginData().getName(), plugin);
    }

    public void pluginEnabled(Plugin plugin) {
        this.enabledPlugins.put(plugin.getPluginData().getName(), plugin);
    }

    public Map<String, Plugin> getEnabledPlugins() {
        return this.enabledPlugins;
    }

    public Map<String, Plugin> getLoadedPlugins() {
        return this.loadedPlugins;
    }
}
