package net.rushnation.rushyprox.util;

public class ServerObject {

    public String name;
    public String ip;
    public int port;

    public ServerObject(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public ServerObject() {
    }

    @Override
    public String toString() {
        return "ServerObject{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
