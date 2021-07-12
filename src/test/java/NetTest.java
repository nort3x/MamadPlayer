import ir.tesla_tic.network.SerializedSocket;
import ir.tesla_tic.network.ServiceDiscovery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetTest {

    @Test void shouldReadAndWrite() throws IOException {
        ServerSocket sv = new ServerSocket(9090);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SerializedSocket incommingSocket = new SerializedSocket(sv.accept());
                    System.out.println(new String(incommingSocket.read()));
                    incommingSocket.write("hi".getBytes());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        SerializedSocket clientSocket = new SerializedSocket(new Socket("127.0.0.1",9090));
        clientSocket.write("salam".getBytes());
        System.out.println(new String(clientSocket.read()));

    }

    @Test void shouldDiscover() throws IOException, InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServiceDiscovery.getDiscovered(new InetSocketAddress("192.168.1.255",9090).getAddress(),"hi","hello","someData");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        String data = ServiceDiscovery.discover(new InetSocketAddress("192.168.1.255",9090).getAddress(),"hi","hello",1000);
        Assertions.assertEquals(data,"someData");

    }

    @Test void shouldDiscoverBulk() throws IOException, InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServiceDiscovery.getDiscoveredOverAllAddresses("hi","hello","someData");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        String data = ServiceDiscovery.discoverOverAllAddresses("hi","hello",1000);

        System.out.println(data);
        Assertions.assertEquals(data,"someData");

    }
}
