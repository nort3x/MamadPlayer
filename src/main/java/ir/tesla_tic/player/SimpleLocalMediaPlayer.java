package ir.tesla_tic.player;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.rmi.RemoteException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleLocalMediaPlayer implements MediaPlayerEntity {

    MediaPlayer mp;
    SimpleDoubleProperty volume = new SimpleDoubleProperty(0.5);
    AudioSpectrumListener audioSpectrumListener = new AudioSpectrumListener() {
        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {

        }
    };
    double interval = 0.1;
    Runnable onStop = new Runnable() {
        @Override
        public void run() {

        }
    };
    BiConsumer<String,Object> meta = new BiConsumer<String, Object>() {
        @Override
        public void accept(String s, Object o) {

        }
    };
    Consumer<Double> current;


    @Override
    public synchronized void play() {
        if(mp!=null)
            mp.play();
    }

    @Override
    public synchronized void pause() {
        if(mp!=null)
            mp.pause();
    }



    @Override
    public synchronized void seekTo(double where) {
        if(mp!=null)
        mp.seek(new Duration(where*mp.getTotalDuration().toMillis()));
    }

    @Override
    public void volumeTo(double where) throws RemoteException {
        mp.setVolume(where);
    }

    @Override
    public synchronized void reInitializeWith(String path) {
        dispose();
        init(path);
        play();
    }

    @Override
    public void currentPositionUpdate(Consumer<Double> r) throws RemoteException {
    current  =r ;
    }


    @Override
    public synchronized void onStopped(Runnable r)  {
        onStop = r;
    }

    @Override
    public synchronized void acceptVolumeBinder(ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        volume.bind(readOnlyDoubleProperty);
    }

    @Override
    public synchronized void onMetaDataChanged(BiConsumer<String, Object> consumer) {
        meta = consumer;
    }

    @Override
    public synchronized void acceptAudioSpectrum(AudioSpectrumListener audioSpectrumListener, double howOftenSeconds)  {
        this.audioSpectrumListener = audioSpectrumListener;
        interval = howOftenSeconds;
    }



    @Override
    public synchronized void dispose(){
        if(mp!=null){
            mp.setOnStopped(null);
            mp.volumeProperty().unbind();
            mp.stop();
            mp.dispose();
            mp = null;
            System.gc();
        }
    }

    private void init(String source){
        mp = new MediaPlayer(new Media(source));
        mp.errorProperty().addListener(new ChangeListener<MediaException>() {
            @Override
            public void changed(ObservableValue<? extends MediaException> observable, MediaException oldValue, MediaException newValue) {
                System.out.println(observable.getValue());
            }
        });
        mp.setOnStopped(onStop);
        mp.setAudioSpectrumListener(audioSpectrumListener);
        mp.setAudioSpectrumInterval(interval);
        mp.volumeProperty().bind(volume);
        mp.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                if(current!=null && mp!=null && mp.getTotalDuration() !=null)
                current.accept(newValue.toMillis()/mp.getTotalDuration().toMillis());
            }
        });
        mp.getMedia().getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(Change<? extends String, ?> change) {
                meta.accept(change.getKey(),change.getValueAdded());
            }
        });
    }

    public void onStartSeekTo(Runnable r){
        mp.setOnPlaying(r);
    }
}
