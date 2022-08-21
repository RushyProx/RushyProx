package net.rushnation.testplugin;

import net.rushnation.rushyprox.event.Event;
import net.rushnation.rushyprox.event.list.ProxyReadyEvent;
import net.rushnation.rushyprox.plugin.*;
import net.rushnation.testplugin.config.TestConfig;

@PluginMain("TestPlugin")
@PluginAuthor("Angekleckert")
@PluginDescription("This is a test plugin for rushyprox")
@PluginVersion("1.0.0")
public class TestPlugin extends Plugin {

    @Override
    public void onLoad() {
        this.getRushyProx().getLogger().info("Hallo ich bin das Testplugin und werde geladen...");
    }

    @Override
    public void onEnable() {
        this.getRushyProx().getLogger().info("Hallo ich bin das Testplugin und werde aktiviert...");
        TestConfig testConfig = new TestConfig();

        getRushyProx().getRushProxScheduler().scheduleSynchronousTask(testConfig::load, this);
        System.out.println(testConfig.wilderInt);
    }

    @Override
    public void onDisable() {
        this.getRushyProx().getLogger().info("Hallo ich bin das Testplugin und werde deaktiviert...");
    }
}
