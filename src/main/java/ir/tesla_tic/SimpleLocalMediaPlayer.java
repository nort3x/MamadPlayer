package ir.tesla_tic;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

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
    double interval = 0.01;
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
    Consumer<Duration> total,current;


    @Override
    public void play() {
        if(mp!=null)
            mp.play();
    }

    @Override
    public void pause() {
        if(mp!=null)
            mp.pause();
    }



    @Override
    public void seekTo(double where) {
        if(mp!=null)
        mp.seek(new Duration(where));
    }

    @Override
    public void reInitializeWith(String path) {
        dispose();
        init(path);
    }


    @Override
    public void totalTimeUpdate(Consumer<Duration> r)  {
        total = r;
    }

    @Override
    public void currentPositionUpdate(Consumer<Duration> r)  {
        current =r;
    }

    @Override
    public void onStopped(Runnable r)  {
        onStop = r;
    }

    @Override
    public void acceptVolumeBinder(ReadOnlyDoubleProperty readOnlyDoubleProperty) {
        volume.bind(readOnlyDoubleProperty);
    }

    @Override
    public void onMetaDataChanged(BiConsumer<String, Object> consumer) {
        meta = consumer;
    }

    @Override
    public void acceptAudioSpectrum(AudioSpectrumListener audioSpectrumListener, double howOftenSeconds)  {
        this.audioSpectrumListener = audioSpectrumListener;
        interval = howOftenSeconds;
    }



    private void dispose(){
        if(mp!=null){
            mp.setOnStopped(()->{});
            mp.volumeProperty().unbind();
            mp.stop();
            mp.dispose();
        }
    }

    private void init(String source){
        mp = new MediaPlayer(new Media(source));
        mp.setOnStopped(onStop);
        mp.setAudioSpectrumListener(audioSpectrumListener);
        mp.setAudioSpectrumInterval(interval);
        mp.volumeProperty().bind(volume);
        mp.totalDurationProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                total.accept(newValue);
            }
        });
        mp.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                current.accept(newValue);
            }
        });
        mp.getMedia().getMetadata().addListener(new MapChangeListener<String, Object>() {
            @Override
            public void onChanged(Change<? extends String, ?> change) {
                meta.accept(change.getKey(),change.getValueAdded());
            }
        });
    }
}
