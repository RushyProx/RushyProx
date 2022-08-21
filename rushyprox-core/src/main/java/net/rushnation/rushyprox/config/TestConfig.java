package net.rushnation.rushyprox.config;

@ConfigFile(fileName = "test.yml", destination = "")
public class TestConfig extends YamlConfig {

    public String bex = "Bex ist mega gay";

    @Comment("Das stimmt halt fr")
    public boolean aliIstGay = true;

    public Integer wilderInt = 100;

    @Comment("Wenn das geht amk ich bin einfach scheiss genie")
    public TestObject haha = new TestObject();

    @Override
    public String toString() {
        return "TestConfig{" +
                "bex='" + bex + '\'' +
                ", aliIstGay=" + aliIstGay +
                ", wilderInt=" + wilderInt +
                ", haha=" + haha.toString() +
                '}';
    }
}
