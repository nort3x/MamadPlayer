import ir.tesla_tic.network.SerializedSocket;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
}
