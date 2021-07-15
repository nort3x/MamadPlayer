package ir.tesla_tic.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import ir.tesla_tic.utils.AmplitudeAverager;
import javafx.scene.media.AudioSpectrumListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SexyArduinoLights {

    private static final char SERIALIZER_BEGIN = 200;
    HashSet<SerialEntity> entities = new HashSet<>();
    private SexyArduinoLights(){
        emitter.setDaemon(true);
        emitter.start();
    }
    Thread scanner;
    public synchronized void start(){
        if(!isRunning) {
         scanner= new Thread(scanForSerials);
            scanner.setDaemon(true);
            isRunning=true;
            scanner.start();
        }
    }
    public synchronized void stop(){
        if(scanner!=null){
            scanner.stop();
        }
        isRunning = false;
    }

    static SexyArduinoLights instance = null;
    public static SexyArduinoLights getInstance(){
        if(instance==null)
            instance = new SexyArduinoLights();
        return instance;
    }

    boolean isRunning = false;
    Runnable scanForSerials = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                Stream.of(SerialPort.getCommPorts()).map(SerialEntity::new).forEach(entities::add);
                try {
                    addConnectOrRemove.forEach(x->{
                        try {
                            if(x.connect())
                                return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        entities.remove(x);
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    private void emit(){
        entities.forEach(x-> {
            try {
                if(x.isConnected())
                    x.write(SERIALIZER_BEGIN,base.get(),mid.get(),high.get());
                else if(!addConnectOrRemove.contains(x))
                    addConnectOrRemove.add(x);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    ConcurrentLinkedQueue<SerialEntity> addConnectOrRemove = new ConcurrentLinkedQueue<>();
    Thread emitter = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true){
                synchronized ( SexyArduinoLights.this){
                    try {
                        SexyArduinoLights.this.wait();
                        emit();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    });

    AtomicInteger base = new AtomicInteger(),mid = new AtomicInteger(),high = new AtomicInteger();
    public  void emitLight(float[] amp){
        float[] arr = AmplitudeAverager.reduced(amp,3);
        base.set((int) (arr[0] +60));
        mid.set((int) (arr[1] +60));
        high.set((int) (arr[2] +60));
        synchronized (this){
            notifyAll();
        }
    }
}
