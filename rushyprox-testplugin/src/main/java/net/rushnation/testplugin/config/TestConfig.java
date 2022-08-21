package net.rushnation.testplugin.config;

import net.rushnation.rushyprox.config.Comment;
import net.rushnation.rushyprox.config.ConfigFile;
import net.rushnation.rushyprox.config.TestObject;
import net.rushnation.rushyprox.config.YamlConfig;

@ConfigFile(fileName = "aligay.yml", destination = "")
public class TestConfig extends YamlConfig {

    public String bex = "Bex ist mega gay";

    @Comment("Das stimmt halt fr")
    public boolean aliIstGay = true;

    public Integer wilderInt = 100;

    @Comment("Wenn das geht amk ich bin einfach scheiss genie")
    public TestObject haha = new TestObject();

}
