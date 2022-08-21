package net.rushnation.rushyprox.config;

public interface IConfig {

    void load() throws ConfigException;

    void save() throws ConfigException;

    void set(String path, Object value) throws ConfigException;

    Object get(String path) throws ConfigException;

    boolean hasConfigFile();

    boolean exists(String path);

}
