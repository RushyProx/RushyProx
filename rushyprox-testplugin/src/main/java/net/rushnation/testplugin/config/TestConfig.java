package net.rushnation.testplugin.config;

import net.rushnation.rushyprox.config.Comment;
import net.rushnation.rushyprox.config.ConfigFile;
import net.rushnation.rushyprox.config.TestObject;
import net.rushnation.rushyprox.config.YamlConfig;

@ConfigFile(fileName = "config.yml", destination = "")
public class TestConfig extends YamlConfig {

    public String test = "Hello World";

    @Comment("Das stimmt halt fr")
    public boolean testBool = true;

    public Integer wilderInt = 100;

    @Comment("This is a comment")
    public TestObject haha = new TestObject();

}
