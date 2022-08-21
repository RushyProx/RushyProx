package net.rushnation.rushyprox.plugin;

import lombok.Getter;
import net.rushnation.rushyprox.RushyProx;

import java.io.File;

@Getter
public class Plugin {

    private PluginData pluginData;
    private RushyProx rushyProx;
    private File pluginFile;

    public void onLoad() {

    }


    public void onEnable() {

    }


    public void onDisable() {

    }

    public void init(PluginData pluginData, RushyProx rushyProx, File jarFile) {
        this.pluginData = pluginData;
        this.rushyProx = rushyProx;
        this.pluginFile = jarFile;
    }

}
