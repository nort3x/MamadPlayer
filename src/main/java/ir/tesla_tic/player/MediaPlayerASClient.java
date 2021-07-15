package ir.tesla_tic.player;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import ir.tesla_tic.model.Command;
import ir.tesla_tic.model.Meta;
import ir.tesla_tic.network.SerializedSocket;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.AudioSpectrumListener;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MediaPlayerASClient implements MediaPlayerEntity {


    SerializedSocket s;
    AtomicBoolean flag = new AtomicBoolean(true);
    ConcurrentLinkedQueue<Command> commandsToSend = new ConcurrentLinkedQueue<>();
    MamadoHTTPServer mamadoHTTPServer = new MamadoHTTPServer();


    AudioSpectrumListener audioSpectrumListener =  new AudioSpectrumListener() {
        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {

        }
    };
    double interval;
    Runnable onStop = new Runnable() {
        @Override
        public void run() {

        }
    };
    BiConsumer<String,Object> meta =  new BiConsumer<String, Object>() {
        @Override
        public void accept(String s, Object o) {

        }
    };
    Consumer<Double> current = new Consumer<Double>() {
        @Override
        public void accept(Double aDouble) {

        }
    };
    SimpleLocalMediaPlayer smp = new SimpleLocalMediaPlayer();


    public MediaPlayerASClient(String host,int port) throws IOException {
        s = new SerializedSocket(new Socket(host,port));


        //writer
        Thread writer = new Thread(new Runnable() {
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
                            flag.set(false);
                        }
                    }
                    try {
                        s.write(writerG.toJson(c).getBytes());
                    } catch (IOException e) {
                        //e.printStackTrace();
                        flag.set(false);
                    }
                }
            }
        });
        writer.setDaemon(true);
        writer.start();



         //reader
        Thread reader = new Thread(new Runnable() {
            Gson readerG = new Gson();
            @Override
            public void run() {
                while (flag.get()) {
                    try {
                        Command c = readerG.fromJson(new String(s.read()),Command.class);
                        switch (c.getT()){
                            case FINISHED:
                                Platform.runLater(()->{
                                    onStop.run();
                                });
                                break;
                            case CURRENT:
                                current.accept(Double.valueOf(c.getMeta_data()));
                                break;
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                        flag.set(false);
                    }
                }
            }
        });
        reader.setDaemon(true);
        reader.start();

        smp.acceptVolumeBinder(new SimpleDoubleProperty(0));

    }

    @Override
    public void dispose() throws RemoteException {
        mamadoHTTPServer.closeAllConnections();
        mamadoHTTPServer.stop();
        try {
            s.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    @Override
    public void play() {
        commandsToSend.add(new Command(Command.Type.PLAY,""));
    }

    @Override
    public void pause() {
        commandsToSend.add(new Command(Command.Type.PAUSE,""));
    }



    @Override
    public void seekTo(double where) {
        commandsToSend.add(new Command(Command.Type.SEEK_TO,String.valueOf(where)));
    }

    @Override
    public void volumeTo(double where) throws RemoteException {
        commandsToSend.add(new Command(Command.Type.VOLUME_TO,String.valueOf(where)));
    }

    @Override
    public void reInitializeWith(String path) throws RemoteException {
        mamadoHTTPServer.initWithFile(path);
        Command debug = new Command(Command.Type.LOAD,
                "http://"+s.getInnerSocket().getLocalAddress().getHostAddress().toString()+":4546/"
        );
        commandsToSend.add(debug);
        try {
            smp.reInitializeWith(new File(path).toURI().toURL().toString());
            smp.mp.setOnPlaying(smp.mp::stop);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void currentPositionUpdate(Consumer<Double> r)  {
        current =r;
    }

    @Override
    public void onStopped(Runnable r)  {
        onStop = r;
    }

    @Override
    public void acceptVolumeBinder(ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        readOnlyDoubleProperty.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                commandsToSend.add(new Command(Command.Type.VOLUME_TO,String.valueOf(newValue.doubleValue())));
            }
        });
    }

    @Override
    public void onMetaDataChanged(BiConsumer<String, Object> consumer) {
        smp.onMetaDataChanged(consumer);
    }

    @Override
    public void acceptAudioSpectrum(AudioSpectrumListener audioSpectrumListener, double howOftenSeconds)  {
        this.audioSpectrumListener = audioSpectrumListener;
        interval = howOftenSeconds;
    }



    private static class MamadoHTTPServer extends NanoHTTPD {

        File serving_file;

        public MamadoHTTPServer() throws IOException {
            super("0.0.0.0",4546);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        }

        public synchronized void initWithFile(String filePath){
            serving_file = new File(filePath);
        }


        @Override
        public Response serve(IHTTPSession session) {
            try {
                FileInputStream fis = new FileInputStream(serving_file);
                return newFixedLengthResponse(Response.Status.OK, "audio/mp3",fis,serving_file.length());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
    }
}
