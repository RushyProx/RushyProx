package net.rushnation.rushyprox.util;

import net.rushnation.rushyprox.config.Comment;
import net.rushnation.rushyprox.config.ConfigFile;
import net.rushnation.rushyprox.config.YamlConfig;

@ConfigFile(fileName = "config.yml", destination = "")
public class RushyProxyConfig extends YamlConfig {

    @Comment("The ip of the proxy")
    public String ip = "0.0.0.0";

    @Comment("The port where the proxy should run")
    public int port = 19132;

    @Comment("The max amount of clients that can connect")
    public int maxClients = 50;

    @Comment("The motd of the proxy")
    public String motd = "RushyProx is cool!";

    @Comment("Should be Xbox Auth enabled? true or false")
    public boolean xboxAuth = true;

    @Comment("This feature may come out soon")
    public boolean sentry = false;
}
