package ir.tesla_tic.player;

import com.google.gson.Gson;
import ir.tesla_tic.model.Command;
import ir.tesla_tic.model.Meta;
import ir.tesla_tic.network.SerializedSocket;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import static ir.tesla_tic.model.Command.Type.*;


public class MediaPlayerASServer {
    ServerSocket sv;
    SerializedSocket s;

    Thread reader,writer;
    AtomicBoolean flag = new AtomicBoolean(false);
    private Object failLock = new Object();

    SimpleDoubleProperty volume = new SimpleDoubleProperty(0.5);
    VLCMediaPlayer smp = new VLCMediaPlayer();
    ConcurrentLinkedQueue<Command> commandsToSend = new ConcurrentLinkedQueue<>();
    public MediaPlayerASServer(ServerSocket sv) throws IOException {
        this.sv= sv;

            smp.acceptVolumeBinder(volume);
            smp.onStopped(()->{
                commandsToSend.add(new Command(FINISHED,""));
            });
            smp.currentPositionUpdate((d)->{
                commandsToSend.add(new Command(CURRENT,String.valueOf(d)));
            });
            smp.onMetaDataChanged((key,value)->{
                if(value instanceof String)
                commandsToSend.add(new Command(META,new Meta(key,(String)value, Meta.Type.STRING).toString()));
                if(value instanceof byte[])
                    commandsToSend.add(new Command(META,new Meta(key,(byte[])value, Meta.Type.STRING).toString()));
            });


    }

    public void doJob() throws Exception {

        commandsToSend.clear();

        flag.set(true);
        if(reader!=null)
            reader.stop();
        if(writer!=null)
            writer.stop();

        s = new SerializedSocket(sv.accept());
        reader = new Thread(new Runnable() {
            Gson readerGson = new Gson();
            @Override
            public void run() {
                while (flag.get()){
                    try {
                        Command c = readerGson.fromJson(new String(s.read()),Command.class);

                            switch (c.getT()) {
                                case PLAY:
                                    smp.play();
                                    break;
                                case PAUSE:
                                    smp.pause();
                                    break;
                                case LOAD:
                                    smp.softDispose();
                                    smp.reInitializeWith("http://"+s.getInnerSocket().getInetAddress().getHostAddress()+":4546/");
                                    break;
                                case SEEK_TO:
                                    smp.seekTo(Double.parseDouble(c.getMeta_data()));
                                    break;
                                case VOLUME_TO:
                                    volume.set(Double.parseDouble(c.getMeta_data()));
                                    break;
                            }
                    } catch (IOException e) {
                        smp.softDispose();
                        //e.printStackTrace();
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
                        //e.printStackTrace();
                        synchronized (failLock){
                            failLock.notifyAll();
                            break;
                        }
                    }
                }
            }
        });


        reader.setDaemon(true);
        writer.setDaemon(true);
        reader.start();
        writer.start();

        synchronized (failLock){
            failLock.wait();
        }
        flag.set(false);
        throw new Exception("errors");
    }
}
