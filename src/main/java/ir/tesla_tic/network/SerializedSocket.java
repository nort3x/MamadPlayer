package ir.tesla_tic.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class SerializedSocket {
    Socket innerSocket;
    OutputStream oups;
    InputStream inps;
    public SerializedSocket(Socket s) throws IOException {
        innerSocket =s;
        innerSocket.setTcpNoDelay(true);
        oups = innerSocket.getOutputStream();
        inps = innerSocket.getInputStream();
    }


    public byte[] read() throws IOException {
        byte[] len = new byte[8];
        if(inps.read(len)!=8)
            throw new IOException();
        byte[] payload = new byte[(int) Converters.bytesToUint64BigEndian(len)];

        int i = 0;
        int k = 0;
        while (true){
            i  = inps.read(payload,i, payload.length-i);
            if(i==-1) {
                throw new IOException();
            }
            else if(k==payload.length) {
                break;
            }

            k+=i;
        }
        return payload;

    }
    public void write(byte[] arr) throws IOException {
        oups.write(Converters.uint64ToBytesBigEndian(arr.length));
        oups.write(arr);
    }


    public void close() throws IOException {
        innerSocket.close();
    }

    public boolean isClosed(){
        return innerSocket.isClosed();
    }

    public Socket getInnerSocket() {
        return innerSocket;
    }
}
