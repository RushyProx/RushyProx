package net.rushnation.rushyprox.util;

import net.rushnation.rushyprox.config.Comment;
import net.rushnation.rushyprox.config.ConfigFile;
import net.rushnation.rushyprox.config.YamlConfig;

@ConfigFile(fileName = "servers.yml", destination = "")
public class ServerConfig extends YamlConfig {

    @Comment("Write down the servers here. They should have 'name', 'ip' and 'port'")
    public ServerObject[] example = new ServerObject[]{new ServerObject("ExampleServer", "0.0.0.0", 1923)};

}
