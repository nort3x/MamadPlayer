package ir.tesla_tic.component;

import com.google.gson.Gson;
import ir.tesla_tic.model.DiscoveredServer;
import ir.tesla_tic.network.ServiceDiscovery;

import java.io.IOException;

public class DiscoverableServer {
    DiscoveredServer ds;
    public DiscoverableServer(DiscoveredServer ds){
        this.ds = ds;
    }

    Gson g = new Gson();
    public void getDiscovered() throws IOException {
        ServiceDiscovery.getDiscoveredOverAllAddresses("mmdPlayer","mmdServer",g.toJson(ds));
    }

}
