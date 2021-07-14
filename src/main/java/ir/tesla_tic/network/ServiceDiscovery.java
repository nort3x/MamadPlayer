package ir.tesla_tic.network;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceDiscovery {

    private static final int MAGIC_PORT = 1729;  // for ramanujan taxi :D

    private ServiceDiscovery() {
    }

    /**
     * Tries to discover server for 1 sec and return null if cannot its caller responsibility to use some loop for further try
     * @param in whichAddress to look on
     * @param servicePing ping msg from client
     * @param servicePong pong msg from server
     * @param expectedDataSize provided data approximate length (try to give upperband)
     * @return provided data from server
     * @throws IOException if cannot make udp socket
     */
    public static String discover(InetAddress in,String servicePing, String servicePong,int expectedDataSize) throws IOException {

        byte[] buffer_send = servicePing.getBytes();
        byte[] buffer_recv = new byte[servicePong.length() + expectedDataSize];

        DatagramSocket ds = new DatagramSocket(MAGIC_PORT+1);
        ds.setReuseAddress(true);
//        ds.bind(new InetSocketAddress(in.getHostName(),MAGIC_PORT+1));
        ds.setBroadcast(true);
        ds.setSoTimeout(1000);

        DatagramPacket dpSend = new DatagramPacket(buffer_send,0,buffer_send.length,in,MAGIC_PORT);

        DatagramPacket dpRecv = new DatagramPacket(buffer_recv, 0,buffer_recv.length);
        dpRecv.setPort(MAGIC_PORT);



            ds.send(dpSend);
            String ans=null;
            try {
                ds.receive(dpRecv);
                String answer = new String(Arrays.copyOfRange(dpRecv.getData(), 0, dpRecv.getLength()));
                if (answer.contains(servicePong)) {
                     ans = answer.replace(servicePong, "");
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        ds.close();
        return ans;
    }

    /**
     * go to blocking mode to answer one discovery call its caller responsibility to use loop for multiple answers
     * @param in
     * @param servicePing
     * @param servicePong
     * @param provideData
     * @throws IOException
     * @see ServiceDiscovery#discover(InetAddress, String, String, int)
     */
    public static void getDiscovered(InetAddress in,String servicePing, String servicePong, String provideData) throws IOException {
        byte[] buffer_recv = new byte[servicePing.length()];
        byte[] buffer_send = (servicePong+provideData).getBytes();

        DatagramSocket ds = new DatagramSocket(MAGIC_PORT);
        ds.setReuseAddress(true);
//        ds.bind(new InetSocketAddress(in.getHostName(),MAGIC_PORT));
        ds.setSoTimeout(0);

        DatagramPacket dpSend = new DatagramPacket(buffer_send, 0,buffer_send.length);
        DatagramPacket dpRecv = new DatagramPacket(buffer_recv, 0,buffer_recv.length);
        dpSend.setPort(MAGIC_PORT+1);

        ds.receive(dpRecv);
        String incoming = new String(Arrays.copyOfRange(dpRecv.getData(), 0, dpRecv.getLength()));
        if (incoming.equals(servicePing)) {
            dpSend.setAddress(dpRecv.getAddress());
            ds.send(dpSend);
        }
        ds.close();

    }

    /**
     * will do a discovery request on each available addresses
     * @param servicePing
     * @param servicePong
     * @param expectedDataSize
     * @return
     * @throws IOException
     */
    public static String discoverOverAllAddresses(String servicePing,String servicePong,int expectedDataSize) throws IOException {
        Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
        ArrayList<InetAddress> addresses = new ArrayList<>();
        while (networkInterface.hasMoreElements()){
            NetworkInterface ni = networkInterface.nextElement();
            if(ni.isLoopback() || !ni.isUp())
                continue;
            ni.getInterfaceAddresses().stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter(Objects::nonNull)
            .forEach(addresses::add);
        }

        String answer = null;
        for (InetAddress address : addresses) {
             answer = discover(address,servicePing,servicePong,expectedDataSize);
            if(answer != null)
                break;
        }

        return answer;

    }

    /**
     * doesnt differ from getDiscover for know its listening on 0.0.0.0
     * @param servicePing
     * @param servicePong
     * @param providedData
     * @throws IOException
     */
    public static void getDiscoveredOverAllAddresses(String servicePing,String servicePong,String providedData) throws IOException {
        Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();
        ArrayList<InetAddress> addresses = new ArrayList<>();
        while (networkInterface.hasMoreElements()){
            NetworkInterface ni = networkInterface.nextElement();
            if(ni.isLoopback() || !ni.isUp())
                continue;
            ni.getInterfaceAddresses().stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter(Objects::nonNull)
                    .forEach(addresses::add);
        }


        //for (InetAddress address : addresses) { //todo
            getDiscovered(addresses.get(0),servicePing,servicePong,providedData);
        //}
    }



}
