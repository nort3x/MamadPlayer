package ir.tesla_tic.network;

import com.google.gson.Gson;
import ir.tesla_tic.SimpleLocalMediaPlayer;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static ir.tesla_tic.network.Command.Type.*;
public class MediaPlayerASServer {
    ServerSocket sv;
    SerializedSocket s;

    Thread reader,writer;
    AtomicBoolean flag = new AtomicBoolean(false);
    private Object failLock = new Object();

    SimpleDoubleProperty volume = new SimpleDoubleProperty(0.5);
    SimpleLocalMediaPlayer smp = new SimpleLocalMediaPlayer();
    ConcurrentLinkedQueue<Command> commandsToSend = new ConcurrentLinkedQueue<>();
    public MediaPlayerASServer(ServerSocket sv) throws IOException {
        this.sv= sv;
        smp.acceptVolumeBinder(volume);
        smp.onStopped(()->{
            commandsToSend.add(new Command(FINISHED,""));
        });
        smp.currentPositionUpdate((d)->{
            commandsToSend.add(new Command(CURRENT,String.valueOf(d.toMillis())));
        });

        smp.totalTimeUpdate((d)->{
            commandsToSend.add(new Command(TOTAL,String.valueOf(d.toMillis())));
        });
    }

    public void doJob() throws Exception {

        flag.set(true);
        if(reader!=null)
            reader.interrupt();
        if(writer!=null)
            writer.interrupt();

        s = new SerializedSocket(sv.accept());
        reader = new Thread(new Runnable() {
            Gson readerGson = new Gson();
            @Override
            public void run() {
                while (flag.get()){
                    try {
                        Command c = readerGson.fromJson(new String(s.read()),Command.class);
                        switch (c.t){
                            case PLAY:
                                smp.play();
                                break;
                            case PAUSE:
                                smp.pause();
                                break;
                            case LOAD:
                                smp.reInitializeWith(c.meta_data);
                                break;
                            case SEEK_TO:
                                smp.seekTo(Double.parseDouble(c.meta_data));
                                break;
                            case VOLUME_TO:
                                volume.set(Double.parseDouble(c.meta_data));
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        synchronized (failLock){
                            failLock.notifyAll();
                            break;
                        }
                    }
                }
            }
        });


        writer = new Thread(new Runnable() {
            Gson writerG = new Gson();
            @Override
            public void run() {
                while (flag.get()){
                    Command c = commandsToSend.poll();
                    if(c == null) {
                        try {
                            Thread.sleep(20);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            synchronized (failLock){
                                failLock.notifyAll();
                                break;
                            }
                        }
                    }
                    try {
                        s.write(writerG.toJson(c).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                        synchronized (failLock){
                            failLock.notifyAll();
                            break;
                        }
                    }
                }
            }
        });


        reader.start();
        writer.start();

        synchronized (failLock){
            failLock.wait();
        }
        flag.set(false);
        throw new Exception("errors");
    }
}
