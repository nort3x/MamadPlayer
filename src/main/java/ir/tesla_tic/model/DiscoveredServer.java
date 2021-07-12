package ir.tesla_tic.model;

public class DiscoveredServer {
    String name;
    String host;
    int port;

    public DiscoveredServer(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }
    public DiscoveredServer(){

    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
