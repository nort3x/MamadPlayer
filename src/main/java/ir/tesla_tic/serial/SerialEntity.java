package ir.tesla_tic.serial;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class SerialEntity{
    SerialPort sp;
    private boolean connected = false;
    public SerialEntity(SerialPort sp)  {
        this.sp  = sp;
        sp.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
        sp.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 10, 10); // block until bytes can be written

    }

    public boolean connect() throws IOException {
        int counter = 5;
        sp.openPort();
        while (counter>0){
            if(sp.isOpen()){
                try {
                    Thread.sleep(10);
                    counter--;
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            sp.getOutputStream().write("ttic_player".getBytes());
            byte[] arr = "yes".getBytes();
            int i = sp.getInputStream().read(arr);
            if(i!="yes".getBytes().length )
                continue;
            else if (Arrays.equals(arr,"yes".getBytes()))
                break;
        }
        if(counter==1)
            throw new IOException();
        connected = true;
        return connected;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerialEntity that = (SerialEntity) o;
        return hashCode()==that.hashCode();
    }

    @Override
    public int hashCode() {
        return sp.getDescriptivePortName().hashCode();
    }

    void write(char flag,int a,int b,int c) throws IOException {
        sp.getOutputStream().write(new byte[]{(byte)flag,(byte)a,(byte)b,(byte)c});
    }
}
